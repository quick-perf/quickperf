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
 * Copyright 2020-2020 the original author or authors.
 */

package org.quickperf.micronaut.micronauttest.sql;

import io.micronaut.configuration.hibernate.jpa.JpaConfiguration;
import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.EachBean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jdbc.DataSourceResolver;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.cfg.AvailableSettings;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

@Factory
public class MicronautStandardServiceRegistryFactory {

    @Inject BeanLocator beanLocator;
    @Inject JpaConfiguration jpaConfiguration;

    @EachBean(DataSource.class)
    @Replaces(StandardServiceRegistry.class)
    protected StandardServiceRegistry hibernateStandardServiceRegistry(
            @Parameter String dataSourceName,
            DataSource dataSource) {

        final DataSourceResolver dataSourceResolver = beanLocator.findBean(DataSourceResolver.class).orElse(null);
        if (dataSourceResolver != null) {
            dataSource = dataSourceResolver.resolve(dataSource);
        }

        Map<String, Object> additionalSettings = new LinkedHashMap<>();
        additionalSettings.put(AvailableSettings.DATASOURCE, wrapDatasource(dataSource));
        additionalSettings.put(
                AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS,
                "org.springframework.orm.hibernate5.SpringSessionContext");
        additionalSettings.put(AvailableSettings.SESSION_FACTORY_NAME, dataSourceName);
        additionalSettings.put(AvailableSettings.SESSION_FACTORY_NAME_IS_JNDI, false);
        JpaConfiguration jpaConfiguration = beanLocator.findBean(JpaConfiguration.class, Qualifiers.byName(dataSourceName))
                .orElse(this.jpaConfiguration);
        return jpaConfiguration.buildStandardServiceRegistry(
                additionalSettings
        );
    }

    public DataSource wrapDatasource(DataSource dataSource){
        final ProxyFactory factory = new ProxyFactory(dataSource);
        factory.setProxyTargetClass(true);
        factory.addAdvice(new ProxyDataSourceInterceptor(dataSource));
        return (DataSource) factory.getProxy();
    }

    private static class ProxyDataSourceInterceptor implements MethodInterceptor {

        private DataSource datasourceProxy;

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
