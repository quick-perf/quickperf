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
package org.quickperf.sql;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

// What QuickPerf does with a test aborted by an assumption depends on when the abort is thrown. Before or by the test
// method, the abort cuts the SQL recording short: the test stays aborted and QuickPerf reports no performance issue.
// After the test method, the recording is complete: QuickPerf evaluates it as for a test without abort.
public class TestAbortedByAnAssumptionJUnit5Test {

    private static void executeSelect(EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
        query.getResultList();
    }

    @QuickPerfTest
    public static class AssumptionFailingOnTheFirstLineOfTheTestMethod extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void execute_one_select_after_an_assumption() {
            assumeTrue(false, "Database not available");
            executeSelect(emf);
        }

    }

    @Test public void
    should_report_a_test_aborted_on_the_first_line_of_its_test_method_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingOnTheFirstLineOfTheTestMethod.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Nothing was measured: no select statement should be reported as missing.%s", errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should stay aborted.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Database not available");

    }

    @QuickPerfTest
    public static class AssumptionFailingInAnAfterEachMethod extends SqlTestBaseJUnit5 {

        @AfterEach
        public void after_each() {
            assumeTrue(false, "Assumption failing in an @AfterEach method");
        }

        @ExpectMaxSelect(1)
        @Test
        public void execute_three_selects() {
            executeSelect(emf);
            executeSelect(emf);
            executeSelect(emf);
        }

    }

    @Test public void
    should_report_the_performance_issue_of_a_test_aborted_after_its_test_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterEachMethod.class;
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                                                                          .selectors(selectClass(testClass))
                                                                          .build();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, listener);

        // THEN
        TestExecutionSummary summary = listener.getSummary();
        String errorReport = errorReportOf(summary);

        assertThat(summary.getTestsFailedCount())
                .as("The SQL recording of the test method is complete: its performance issue should fail the test.%s"
                  , errorReport)
                .isOne();

        assertThat(errorReport).contains("You may think that at most <1> select statement was sent to the database")
                               .contains("But there are in fact <3>...");

        // QuickPerf reports the performance issue before the execution of the @AfterEach method: JUnit adds the abort
        // to the failure as a suppressed exception. The error report does not contain its message, because JUnit
        // Platform 1.6 prints a suppressed exception without its message when the failure has no stack trace.
        Throwable failure = summary.getFailures().get(0).getException();
        assertThat(suppressedExceptionsOf(failure))
                .as("The abort thrown by the @AfterEach method should be reported with the performance issue.%s"
                  , errorReport)
                .contains("Assumption failing in an @AfterEach method");

    }

    private static String errorReportOf(TestExecutionSummary summary) {
        StringWriter errorReport = new StringWriter();
        summary.printFailuresTo(new PrintWriter(errorReport));
        return errorReport.toString();
    }

    private static String suppressedExceptionsOf(Throwable throwable) {
        StringBuilder suppressedExceptions = new StringBuilder();
        for (Throwable suppressed : throwable.getSuppressed()) {
            suppressedExceptions.append(suppressed).append(System.lineSeparator());
        }
        return suppressedExceptions.toString();
    }

    @QuickPerfTest
    public static class AssumptionFailingInABeforeEachMethod extends SqlTestBaseJUnit5 {

        @BeforeEach
        public void before_each() {
            assumeTrue(false, "Assumption failing in a @BeforeEach method");
        }

        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            executeSelect(emf);
        }

    }

    @Test public void
    should_report_a_test_aborted_before_its_test_method_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeEachMethod.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test method was not executed: no select statement should be reported as missing.%s"
                  , errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should stay aborted.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing in a @BeforeEach method");

    }

    // The cost of deciding by the time of the abort: the select statements executed before the abort exceed the
    // maximum whatever the rest of the test method does, but the test stays aborted without reporting them
    @QuickPerfTest
    public static class AssumptionFailingInTheTestMethodAfterSelects extends SqlTestBaseJUnit5 {

        @ExpectMaxSelect(1)
        @Test
        public void execute_three_selects_before_an_assumption() {
            executeSelect(emf);
            executeSelect(emf);
            executeSelect(emf);
            assumeTrue(false, "Assumption failing after the select statements");
        }

    }

    @Test public void
    should_not_report_the_select_statements_executed_before_an_abort_in_the_test_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInTheTestMethodAfterSelects.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The abort cut the SQL recording short: the test should stay aborted.%s", errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should stay aborted.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing after the select statements");

    }

}
