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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/*
Inspiration from https://blog.arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
and https://github.com/gavlyukovskiy/spring-boot-data-source-decorator
*/
public class QuickPerfProxyBeanPostProcessor implements BeanPostProcessor, Ordered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
            final ProxyFactory factory = new ProxyFactory(bean);
            factory.setProxyTargetClass(true);
            factory.addAdvice(new ProxyDataSourceInterceptor((DataSource) bean));
            return factory.getProxy();
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
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
                                                                  ,invocation.getMethod().getName());
            if (proxyMethod != null) {
                return proxyMethod.invoke(this.datasourceProxy, invocation.getArguments());
            }
            return invocation.proceed();
        }
    }

}
