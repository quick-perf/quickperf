/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.spring.sql;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Inspiration from https://blog.arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
// and https://github.com/gavlyukovskiy/spring-boot-data-source-decorator

// Implements PriorityOrdered (not Ordered) so this BeanPostProcessor is registered before any
// Ordered BeanPostProcessor that depends on the DataSource. Spring instantiates PriorityOrdered
// bean post-processors first.

// Declare this bean with a static @Bean method or in XML. A @Bean method must declare
// QuickPerfProxyBeanPostProcessor as its return type, because Spring looks for PriorityOrdered on
// that type before creating the bean. With a non-static @Bean method, Spring creates the declaring
// @Configuration class before registering the bean post-processors that handle @Autowired, @Value
// and @PostConstruct, so these are ignored there.
public class QuickPerfProxyBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && !isProxyDataSourceBean(bean)) {
            final ProxyFactory factory = new ProxyFactory(bean);
            factory.setProxyTargetClass(true);
            factory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
            return factory.getProxy();
        }
        return bean;
    }

    private boolean isProxyDataSourceBean(Object bean) {
        return bean.toString().contains(ProxyDataSource.class.getName());
    }

    @Override
    public int getOrder() {
        return PriorityOrdered.LOWEST_PRECEDENCE - 1;
    }

    private static class ProxyDataSourceInterceptor implements MethodInterceptor {

        private final DataSource datasourceProxy;

        public ProxyDataSourceInterceptor(final DataSource dataSource) {
            this.datasourceProxy =
                    QuickPerfSqlDataSourceBuilder.aDataSourceBuilder()
                    .buildProxy(dataSource);
        }

        @Override
        public Object invoke(final MethodInvocation invocation) throws Throwable {
            Method proxyMethod = ReflectionUtils.findMethod( this.datasourceProxy.getClass()
                                                           , invocation.getMethod().getName());
            if (proxyMethod != null) {
                // Unwrapped so that the caller gets what the DataSource threw, such as an SQLException.
                try {
                    return proxyMethod.invoke(this.datasourceProxy, invocation.getArguments());
                }
                catch (InvocationTargetException e) {
                    throw e.getTargetException();
                }
            }
            return invocation.proceed();
        }
    }

}
