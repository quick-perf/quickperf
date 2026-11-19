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
package org.quickperf.spring.springboottest;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.spring.springboottest.controller.CrossTestContaminationWithConcurrentAsync;
import org.quickperf.spring.springboottest.controller.CrossTestContaminationWithConcurrentRandomPort;
import org.quickperf.spring.springboottest.controller.DetectionOfNPlusOneSelectWithAsync;
import org.quickperf.spring.springboottest.controller.DetectionOfNPlusOneSelectWithCompletableFuture;
import org.quickperf.spring.springboottest.service.DetectionOfSelectWithScheduled;
import org.quickperf.sql.SqlExecutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class SpringBootThreadingScenariosJunit5Test {

    @Test
    void should_fail_if_select_number_is_greater_than_expected_with_async() {

        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectWithAsync.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport)
                      .contains("You may think that <1> select statement was sent to the database")
                      .contains("Perhaps you are facing an N+1 select issue");

    }

    @Test
    void should_fail_if_select_number_is_greater_than_expected_with_completable_future() {

        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectWithCompletableFuture.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport)
                      .contains("You may think that <1> select statement was sent to the database")
                      .contains("Perhaps you are facing an N+1 select issue");

    }

    @Test
    void should_detect_select_from_scheduled_task() {

        // GIVEN
        Class<?> testClass = DetectionOfSelectWithScheduled.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport)
                      .contains("You may think that <0> select statement was sent to the database")
                      .contains("But there is in fact <1>");

    }

    @Test
    void concurrent_tests_with_async() {

        // GIVEN
        TestExecutionSummary summary =
                runInParallel(CrossTestContaminationWithConcurrentAsync.class);

        // THEN
        // Active-set fallback attributes the @Async worker's SELECTs to BOTH
        // concurrent recorders and flags both of them as contaminated: both
        // tests fail with the cross-test contamination error, including
        // test_with_async_select whose @ExpectSelect(3) is respected.
        assertThat(summary.getTestsFailedCount())
                      .isEqualTo(2);

        assertThat(failureMessageOf(summary, "test_with_async_select"))
                      .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                      .doesNotContain("You may think that");

        // test_with_no_sql (@ExpectSelect(0)) also sees the sibling test's SQL
        assertThat(failureMessageOf(summary, "test_with_no_sql"))
                      .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                      .contains("You may think that <0> select statement was sent to the database");

    }

    @Test
    void concurrent_tests_with_random_port() {

        // GIVEN
        TestExecutionSummary summary =
                runInParallel(CrossTestContaminationWithConcurrentRandomPort.class);

        // THEN
        // Active-set fallback attributes the Tomcat worker's SELECTs to BOTH
        // concurrent recorders and flags both of them as contaminated: both
        // tests fail with the cross-test contamination error, including
        // test_with_select whose @ExpectSelect(3) is respected.
        assertThat(summary.getTestsFailedCount())
                      .isEqualTo(2);

        assertThat(failureMessageOf(summary, "test_with_select"))
                      .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                      .doesNotContain("You may think that");

        // test_with_no_sql (@ExpectSelect(0)) also sees the sibling test's SQL
        assertThat(failureMessageOf(summary, "test_with_no_sql"))
                      .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                      .contains("You may think that <0> select statement was sent to the database");

    }

    private static TestExecutionSummary runInParallel(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "2")
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);
        return listener.getSummary();
    }

    /** Concatenates the exception message (and its cause chain) of the
     * failure of the given test method so tests can grep for the cross-test
     * contamination error that QuickPerf reports as a perf issue. */
    private static String failureMessageOf(TestExecutionSummary summary, String testMethodName) {
        StringBuilder sb = new StringBuilder();
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            if (!failure.getTestIdentifier().getDisplayName().startsWith(testMethodName + "(")) {
                continue;
            }
            Throwable t = failure.getException();
            while (t != null) {
                if (t.getMessage() != null) {
                    sb.append(t.getMessage()).append(System.lineSeparator());
                }
                t = t.getCause();
            }
        }
        return sb.toString();
    }

}
