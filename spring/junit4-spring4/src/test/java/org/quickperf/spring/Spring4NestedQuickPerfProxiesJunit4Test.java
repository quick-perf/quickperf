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
package org.quickperf.spring;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.quickperf.jvm.JVM;
import org.quickperf.spring.nestedproxies.LazyDataSourceAroundAQuickPerfProxy;
import org.quickperf.spring.nestedproxies.QuickPerfProxyBean;
import org.quickperf.spring.nestedproxies.RoutingDataSourceWithDataSourceBeans;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;
import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.assertThatTheEventsOfOneSelectAreProfiledOnce;

public class Spring4NestedQuickPerfProxiesJunit4Test {

    // The CGLIB of Spring 4 cannot define classes from Java 16
    @Before
    public void assume_that_the_jvm_is_older_than_java_16() {
        assumeTrue(JVM.INSTANCE.version.isLessThanTo16());
    }

    @Test
    public void should_not_proxy_a_quickperf_proxy_bean_again() throws IOException {

        // GIVEN
        Class<?> testClass = QuickPerfProxyBean.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(QuickPerfProxyBean.PROFILING_FILE_PATH);

    }

    @Test
    public void should_profile_the_connection_events_once_through_a_datasource_bean_delegating_to_a_quickperf_proxy() throws IOException {

        // GIVEN
        Class<?> testClass = LazyDataSourceAroundAQuickPerfProxy.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(LazyDataSourceAroundAQuickPerfProxy.PROFILING_FILE_PATH);

    }

    @Test
    public void should_record_a_select_once_through_a_routing_datasource_with_datasource_beans() throws IOException {

        // GIVEN
        Class<?> testClass = RoutingDataSourceWithDataSourceBeans.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(RoutingDataSourceWithDataSourceBeans.PROFILING_FILE_PATH);

    }

}
