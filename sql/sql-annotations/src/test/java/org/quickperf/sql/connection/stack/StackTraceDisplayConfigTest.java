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
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StackTraceDisplayConfigTest {

    // Top of the stack trace of Connection.prepareStatement(String), with Java 8 element names
    private static final StackTraceElement[] QUICKPERF_ELEMENTS = {
            new StackTraceElement("java.lang.Thread", "getStackTrace", "Thread.java", 1559),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "findCurrentStackTrace", "ConnectionProfiler.java", 72),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "printStackTrace", "ConnectionProfiler.java", 64),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionProfiler", "profile", "ConnectionProfiler.java", 51),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionEventsProfiler", "profileForTraceLevel", "ConnectionEventsProfiler.java", 74),
            new StackTraceElement("org.quickperf.sql.connection.ConnectionEventsProfiler", "prepareStatement", "ConnectionEventsProfiler.java", 84),
            new StackTraceElement("org.quickperf.sql.connection.QuickPerfDatabaseConnection", "prepareStatement", "QuickPerfDatabaseConnection.java", 61)
    };

    private static final StackTraceElement REPOSITORY_ELEMENT
            = new StackTraceElement("org.example.BookRepository", "findAll", "BookRepository.java", 31);

    private static final StackTraceElement SERVICE_ELEMENT
            = new StackTraceElement("org.example.BookService", "findBooks", "BookService.java", 24);

    private static final StackTraceElement TEST_METHOD_ELEMENT
            = new StackTraceElement("org.example.BookServiceTest", "should_find_books", "BookServiceTest.java", 42);

    // Simplified Java 8 stack trace of Connection.prepareStatement(String) called from a JUnit 5 test method
    private static final StackTraceElement[] JUNIT5_STACK_TRACE = concat(QUICKPERF_ELEMENTS, new StackTraceElement[] {
            REPOSITORY_ELEMENT,
            SERVICE_ELEMENT,
            TEST_METHOD_ELEMENT,
            new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke0", null, -2),
            new StackTraceElement("sun.reflect.NativeMethodAccessorImpl", "invoke", "NativeMethodAccessorImpl.java", 62),
            new StackTraceElement("sun.reflect.DelegatingMethodAccessorImpl", "invoke", "DelegatingMethodAccessorImpl.java", 43),
            new StackTraceElement("java.lang.reflect.Method", "invoke", "Method.java", 498),
            new StackTraceElement("org.junit.platform.commons.util.ReflectionUtils", "invokeMethod", "ReflectionUtils.java", 686),
            new StackTraceElement("org.junit.jupiter.engine.execution.MethodInvocation", "proceed", "MethodInvocation.java", 60),
            new StackTraceElement("org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation", "proceed", "InvocationInterceptorChain.java", 131),
            new StackTraceElement("org.quickperf.junit5.QuickPerfTestExtension", "interceptTestMethod", "QuickPerfTestExtension.java", 141),
            new StackTraceElement("org.junit.jupiter.engine.execution.ExecutableInvoker", "invoke", "ExecutableInvoker.java", 115),
            new StackTraceElement("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "invokeTestMethod", "TestMethodTestDescriptor.java", 208),
            new StackTraceElement("org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor", "execute", "TestMethodTestDescriptor.java", 137),
            new StackTraceElement("org.junit.platform.engine.support.hierarchical.NodeTestTask", "executeRecursively", "NodeTestTask.java", 151),
            new StackTraceElement("org.junit.platform.engine.support.hierarchical.NodeTestTask", "execute", "NodeTestTask.java", 95),
            new StackTraceElement("org.junit.platform.launcher.core.DefaultLauncher", "execute", "DefaultLauncher.java", 248),
            new StackTraceElement("org.apache.maven.surefire.junitplatform.JUnitPlatformProvider", "invoke", "JUnitPlatformProvider.java", 142),
            new StackTraceElement("org.apache.maven.surefire.booter.ForkedBooter", "main", "ForkedBooter.java", 418)
    });

    private static final StackTraceElement[] THREAD_POOL_ELEMENTS = {
            REPOSITORY_ELEMENT,
            new StackTraceElement("org.example.BookExportTask", "run", "BookExportTask.java", 18),
            new StackTraceElement("java.util.concurrent.ThreadPoolExecutor", "runWorker", "ThreadPoolExecutor.java", 1149),
            new StackTraceElement("java.util.concurrent.ThreadPoolExecutor$Worker", "run", "ThreadPoolExecutor.java", 624),
            new StackTraceElement("java.lang.Thread", "run", "Thread.java", 748)
    };

    // Java 8 stack trace of Connection.prepareStatement(String) called from a thread pool, without test framework elements
    private static final StackTraceElement[] THREAD_POOL_STACK_TRACE = concat(QUICKPERF_ELEMENTS, THREAD_POOL_ELEMENTS);

    @Test public void
    should_limit_the_filtered_stack_trace_to_the_stack_depth() {

        // GIVEN
        StackTrace.StackDepth stackDepth = new StackTrace.StackDepth((short) 2);
        StackTraceDisplayConfig stackTraceDisplayConfig = buildConfigFilteringStackTrace(stackDepth);

        // WHEN
        StackTraceElement[] formattedStackTrace = stackTraceDisplayConfig.format(JUNIT5_STACK_TRACE);

        // THEN
        assertThat(formattedStackTrace).containsExactly(REPOSITORY_ELEMENT, SERVICE_ELEMENT);

    }

    @Test public void
    should_not_display_the_filtered_stack_trace_elements_if_the_stack_depth_is_greater_than_the_number_of_remaining_elements() {

        // GIVEN
        StackTrace.StackDepth stackDepth = new StackTrace.StackDepth((short) 5);
        StackTraceDisplayConfig stackTraceDisplayConfig = buildConfigFilteringStackTrace(stackDepth);

        // WHEN
        StackTraceElement[] formattedStackTrace = stackTraceDisplayConfig.format(JUNIT5_STACK_TRACE);

        // THEN
        assertThat(formattedStackTrace).containsExactly(REPOSITORY_ELEMENT, SERVICE_ELEMENT, TEST_METHOD_ELEMENT);

    }

    @Test public void
    should_not_display_null_elements_if_the_stack_depth_goes_beyond_the_end_of_the_stack_trace() {

        // GIVEN
        StackTrace.StackDepth stackDepth = new StackTrace.StackDepth((short) 8);
        StackTraceDisplayConfig stackTraceDisplayConfig = buildConfigFilteringStackTrace(stackDepth);

        // WHEN
        StackTraceElement[] formattedStackTrace = stackTraceDisplayConfig.format(THREAD_POOL_STACK_TRACE);

        // THEN
        assertThat(formattedStackTrace).containsExactly(THREAD_POOL_ELEMENTS);

    }

    @Test public void
    should_display_all_the_remaining_elements_of_the_filtered_stack_trace_if_the_stack_depth_is_not_limited() {

        // GIVEN
        StackTraceDisplayConfig stackTraceDisplayConfig = buildConfigFilteringStackTrace(StackTrace.StackDepth.ALL);

        // WHEN
        StackTraceElement[] formattedStackTrace = stackTraceDisplayConfig.format(JUNIT5_STACK_TRACE);

        // THEN
        assertThat(formattedStackTrace).containsExactly(REPOSITORY_ELEMENT, SERVICE_ELEMENT, TEST_METHOD_ELEMENT);

    }

    @Test public void
    should_limit_the_unfiltered_stack_trace_to_the_stack_depth() {

        // GIVEN
        StackTrace.StackDepth stackDepth = new StackTrace.StackDepth((short) 5);
        List<StackTraceFilter> noStackTraceFilter = Collections.emptyList();
        StackTraceDisplayConfig stackTraceDisplayConfig = StackTraceDisplayConfig.of(stackDepth, noStackTraceFilter);

        // WHEN
        StackTraceElement[] formattedStackTrace = stackTraceDisplayConfig.format(JUNIT5_STACK_TRACE);

        // THEN
        assertThat(formattedStackTrace).containsExactly(Arrays.copyOfRange(JUNIT5_STACK_TRACE, 0, 5));

    }

    private static StackTraceDisplayConfig buildConfigFilteringStackTrace(StackTrace.StackDepth stackDepth) {
        List<StackTraceFilter> stackTraceFilters = Arrays.<StackTraceFilter>asList( QuickPerfStackTraceTraceFilter.INSTANCE
                                                                                  , JUnit4StackTraceFilter.INSTANCE
                                                                                  , JUnit5StackTraceFilter.INSTANCE
                                                                                  , TestNGStackTraceFilter.INSTANCE);
        return StackTraceDisplayConfig.of(stackDepth, stackTraceFilters);
    }

    private static StackTraceElement[] concat(StackTraceElement[] firstElements, StackTraceElement[] lastElements) {
        List<StackTraceElement> elements = new ArrayList<>(Arrays.asList(firstElements));
        elements.addAll(Arrays.asList(lastElements));
        return elements.toArray(new StackTraceElement[0]);
    }

}
