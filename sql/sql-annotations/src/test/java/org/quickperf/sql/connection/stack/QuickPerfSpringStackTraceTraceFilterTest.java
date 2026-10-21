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
package org.quickperf.sql.connection.stack;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfSpringStackTraceTraceFilterTest {

    // Top of the stack trace of DataSource.getConnection() on a DataSource proxied by QuickPerfProxyBeanPostProcessor,
    // captured with JDK 17
    private static final StackTraceElement[] QUICKPERF_ELEMENTS = {
            new StackTraceElement("java.lang.Thread", "getStackTrace", "Thread.java", 1610),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "findCurrentStackTrace", "ConnectionProfiler.java", 72),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "printStackTrace", "ConnectionProfiler.java", 64),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "profile", "ConnectionProfiler.java", 51),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionEventsProfiler", "theDatasourceGetsTheConnection", "ConnectionEventsProfiler.java", 51),
            new StackTraceElement("org.quickperf.sql.connection.QuickPerfDatabaseConnection", "theDatasourceGetsTheConnection", "QuickPerfDatabaseConnection.java", 40),
            new StackTraceElement("org.quickperf.sql.config.library.QuickPerfProxyDataSource", "getConnection", "QuickPerfProxyDataSource.java", 27),
            new StackTraceElement("jdk.internal.reflect.NativeMethodAccessorImpl", "invoke0", "NativeMethodAccessorImpl.java", -2),
            new StackTraceElement("jdk.internal.reflect.NativeMethodAccessorImpl", "invoke", "NativeMethodAccessorImpl.java", 77),
            new StackTraceElement("jdk.internal.reflect.DelegatingMethodAccessorImpl", "invoke", "DelegatingMethodAccessorImpl.java", 43),
            new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 568),
            new StackTraceElement("org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor$ProxyDataSourceInterceptor", "invoke", "QuickPerfProxyBeanPostProcessor.java", 160)
    };

    // Callers of the DataSource proxy, through the Spring AOP proxy of a @Transactional service.
    // These Spring AOP elements have to stay in the stack trace.
    private static final StackTraceElement[] CALLER_ELEMENTS = {
            new StackTraceElement("org.example.BookRepository", "findAll", "BookRepository.java", 31),
            new StackTraceElement("org.springframework.transaction.interceptor.TransactionInterceptor", "invoke", "TransactionInterceptor.java", 119),
            new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed", "ReflectiveMethodInvocation.java", 186),
            new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor", "intercept", "CglibAopProxy.java", 688),
            new StackTraceElement("org.example.BookService$$EnhancerBySpringCGLIB$$9f2c3e1a", "findBooks", "<generated>", -1)
    };

    @Test public void
    should_start_the_stack_trace_with_the_data_source_proxy_if_two_spring_aop_elements_call_the_interceptor() {

        // GIVEN
        // CGLIB proxy, Spring 3.2 to 5.1 and Spring 6.2+
        StackTraceElement[] springAopElements = {
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed", "ReflectiveMethodInvocation.java", 186),
                new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor", "intercept", "CglibAopProxy.java", 688)
        };
        StackTraceElement dataSourceProxyElement
                = new StackTraceElement("com.zaxxer.hikari.HikariDataSource$$EnhancerBySpringCGLIB$$2e424f09", "getConnection", "<generated>", -1);
        StackTraceElement[] stackTrace = buildGetConnectionStackTrace(springAopElements, dataSourceProxyElement);

        // WHEN
        StackTraceElement[] formattedStackTrace = buildConfigFilteringStackTrace().format(stackTrace);

        // THEN
        assertThat(formattedStackTrace).containsExactly(concat(new StackTraceElement[] {dataSourceProxyElement}, CALLER_ELEMENTS));

    }

    @Test public void
    should_start_the_stack_trace_with_the_data_source_proxy_if_three_spring_aop_elements_call_the_interceptor() {

        // GIVEN
        // CGLIB proxy, Spring 5.2 to 6.1
        StackTraceElement[] springAopElements = {
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed", "ReflectiveMethodInvocation.java", 184),
                new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation", "proceed", "CglibAopProxy.java", 768),
                new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor", "intercept", "CglibAopProxy.java", 720)
        };
        StackTraceElement dataSourceProxyElement
                = new StackTraceElement("com.zaxxer.hikari.HikariDataSource$$SpringCGLIB$$0", "getConnection", "<generated>", -1);
        StackTraceElement[] stackTrace = buildGetConnectionStackTrace(springAopElements, dataSourceProxyElement);

        // WHEN
        StackTraceElement[] formattedStackTrace = buildConfigFilteringStackTrace().format(stackTrace);

        // THEN
        assertThat(formattedStackTrace).containsExactly(concat(new StackTraceElement[] {dataSourceProxyElement}, CALLER_ELEMENTS));

    }

    @Test public void
    should_start_the_stack_trace_with_the_data_source_proxy_if_it_is_a_jdk_dynamic_proxy() {

        // GIVEN
        StackTraceElement[] springAopElements = {
                new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed", "ReflectiveMethodInvocation.java", 184),
                new StackTraceElement("org.springframework.aop.framework.JdkDynamicAopProxy", "invoke", "JdkDynamicAopProxy.java", 223)
        };
        StackTraceElement dataSourceProxyElement
                = new StackTraceElement("com.sun.proxy.$Proxy118", "getConnection", null, -1);
        StackTraceElement[] stackTrace = buildGetConnectionStackTrace(springAopElements, dataSourceProxyElement);

        // WHEN
        StackTraceElement[] formattedStackTrace = buildConfigFilteringStackTrace().format(stackTrace);

        // THEN
        assertThat(formattedStackTrace).containsExactly(concat(new StackTraceElement[] {dataSourceProxyElement}, CALLER_ELEMENTS));

    }

    private static StackTraceElement[] buildGetConnectionStackTrace(StackTraceElement[] springAopElements, StackTraceElement dataSourceProxyElement) {
        StackTraceElement[] elementsUpToDataSourceProxy = concat( concat(QUICKPERF_ELEMENTS, springAopElements)
                                                                , new StackTraceElement[] {dataSourceProxyElement});
        return concat(elementsUpToDataSourceProxy, CALLER_ELEMENTS);
    }

    private static StackTraceDisplayConfig buildConfigFilteringStackTrace() {
        List<StackTraceFilter> stackTraceFilters = Arrays.<StackTraceFilter>asList( QuickPerfStackTraceTraceFilter.INSTANCE
                                                                                  , QuickPerfSpringStackTraceTraceFilter.INSTANCE);
        return StackTraceDisplayConfig.of(StackTrace.StackDepth.ALL, stackTraceFilters);
    }

    private static StackTraceElement[] concat(StackTraceElement[] firstElements, StackTraceElement[] lastElements) {
        List<StackTraceElement> elements = new ArrayList<>(Arrays.asList(firstElements));
        elements.addAll(Arrays.asList(lastElements));
        return elements.toArray(new StackTraceElement[0]);
    }

}
