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
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ReflectionUtils;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


// Inspiration from https://blog.arnoldgalovics.com/configuring-a-datasource-proxy-in-spring-boot/
// and https://github.com/gavlyukovskiy/spring-boot-data-source-decorator

// Implements PriorityOrdered (not Ordered) so this BeanPostProcessor is registered before any
// Ordered BeanPostProcessor that depends on the DataSource. Spring instantiates PriorityOrdered
// bean post-processors first.

// Declare this bean with a static @Bean method, in XML, or by importing QuickPerfSqlConfig. A @Bean
// method must declare QuickPerfProxyBeanPostProcessor as its return type, because Spring looks for
// PriorityOrdered on that type before creating the bean. With a non-static @Bean method, Spring
// creates the declaring @Configuration class before registering the bean post-processors that
// handle @Autowired, @Value and @PostConstruct: they are ignored there, and constructor
// injection fails.
public class QuickPerfProxyBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    // Bounds the walk so that DataSource beans pointing at each other cannot loop forever.
    private static final int MAX_DELEGATION_DEPTH = 16;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof DataSource && !ScopedProxyUtils.isScopedTarget(beanName)) {
            DataSource dataSource = (DataSource) bean;
            if (isAlreadyProxied(dataSource)) {
                proxyReadOnlyDataSourcesAlong(dataSource);
                return bean;
            }
            return proxy(dataSource, true);
        }
        return bean;
    }

    private DataSource proxy(DataSource dataSource, boolean proxyTargetClass) {
        final ProxyFactory factory = new ProxyFactory(dataSource);
        factory.setProxyTargetClass(proxyTargetClass);
        factory.addInterface(QuickPerfProxiedDataSource.class);
        factory.addAdvice(new ProxyDataSourceInterceptor(dataSource));
        return (DataSource) factory.getProxy();
    }

    // LazyConnectionDataSourceProxy sends read-only connections to a DataSource that getTargetDataSource
    // does not reach, and delegates that are not beans can hold one too, so the whole chain is visited.
    private void proxyReadOnlyDataSourcesAlong(DataSource dataSource) {
        DataSource currentDataSource = dataSource;
        for (int depth = 0;
             currentDataSource != null && !(currentDataSource instanceof QuickPerfProxiedDataSource) && depth < MAX_DELEGATION_DEPTH;
             depth++) {
            proxyReadOnlyDataSourceOf(currentDataSource);
            currentDataSource = targetDataSourceOf(currentDataSource);
        }
    }

    // Proxied in place so read-only queries are recorded once; a failure leaves it untouched. The setter
    // takes a DataSource, so an interface proxy is enough and also works when the class is final.
    private void proxyReadOnlyDataSourceOf(DataSource dataSource) {
        Field readOnlyDataSourceField = ReflectionUtils.findField(dataSource.getClass(), "readOnlyDataSource", DataSource.class);
        Method readOnlyDataSourceSetter = ReflectionUtils.findMethod(dataSource.getClass(), "setReadOnlyDataSource", DataSource.class);
        if (readOnlyDataSourceField == null || readOnlyDataSourceSetter == null) {
            return;
        }
        try {
            ReflectionUtils.makeAccessible(readOnlyDataSourceField);
            ReflectionUtils.makeAccessible(readOnlyDataSourceSetter);
            Object readOnlyDataSource = ReflectionUtils.getField(readOnlyDataSourceField, dataSource);
            if (readOnlyDataSource instanceof DataSource && !isAlreadyProxied((DataSource) readOnlyDataSource)) {
                ReflectionUtils.invokeMethod(readOnlyDataSourceSetter, dataSource, proxy((DataSource) readOnlyDataSource, false));
            }
        }
        catch (RuntimeException ignored) {
        }
    }

    // A delegate is proxied before the bean delegating to it is created, so proxying both would
    // record every query twice. Skipping the delegating bean still records the queries: they go
    // through the proxied delegate.
    private boolean isAlreadyProxied(DataSource dataSource) {
        DataSource currentDataSource = dataSource;
        for (int depth = 0; currentDataSource != null && depth < MAX_DELEGATION_DEPTH; depth++) {
            if (currentDataSource instanceof QuickPerfProxiedDataSource) {
                return true;
            }
            currentDataSource = targetDataSourceOf(currentDataSource);
        }
        return false;
    }

    // The accessor is looked up rather than reached through DelegatingDataSource because
    // spring-jdbc is not a dependency of this module. Calling it must never break the
    // application context, so a failure falls back to proxying the bean.
    private DataSource targetDataSourceOf(DataSource dataSource) {
        Method targetDataSourceMethod = ReflectionUtils.findMethod(dataSource.getClass(), "getTargetDataSource");
        if (targetDataSourceMethod == null
                || !DataSource.class.isAssignableFrom(targetDataSourceMethod.getReturnType())) {
            return null;
        }
        try {
            ReflectionUtils.makeAccessible(targetDataSourceMethod);
            Object targetDataSource = ReflectionUtils.invokeMethod(targetDataSourceMethod, dataSource);
            return targetDataSource instanceof DataSource ? (DataSource) targetDataSource : null;
        }
        catch (RuntimeException e) {
            return null;
        }
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
                                                                  ,invocation.getMethod().getName());
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
