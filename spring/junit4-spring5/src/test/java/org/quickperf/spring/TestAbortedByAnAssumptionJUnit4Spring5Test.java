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

import org.junit.Assume;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.spring.database.SpringConfig;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

// An assumption failing before or in the test method cuts the SQL recording short: the test stays skipped, and
// QuickPerf reports no performance issue.
public class TestAbortedByAnAssumptionJUnit4Spring5Test {

    private static void executeSelect(DataSource dataSource) {
        new JdbcTemplate(dataSource).queryForObject("SELECT 1", Integer.class);
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(classes = SpringConfig.class)
    public static class AssumptionFailingOnTheFirstLineOfTheTestMethod {

        @Autowired
        private DataSource dataSource;

        @ExpectSelect(1)
        @Test
        public void execute_one_select_after_an_assumption() {
            Assume.assumeTrue("Database not available", false);
            executeSelect(dataSource);
        }

    }

    @Test public void
    should_report_a_test_aborted_on_the_first_line_of_its_test_method_as_skipped() {

        // GIVEN
        Class<?> testClass = AssumptionFailingOnTheFirstLineOfTheTestMethod.class;

        // WHEN
        JUnit4TestsResult testsResult = JUnit4TestsResult.run(testClass);

        // THEN
        String errorReport = testsResult.getErrorReport();

        assertThat(testsResult.getNumberOfFailures())
                .as("Nothing was measured: no select statement should be reported as missing.%s", errorReport)
                .isZero();

        assertThat(testsResult.getNumberOfAssumptionFailures())
                .as("The test should stay skipped.%s", errorReport)
                .isOne();

        assertThat(testsResult.getAssumptionFailureReport())
                .contains("Database not available");

    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(classes = SpringConfig.class)
    public static class AssumptionFailingOnTheFirstLineOfATestMethodExecutedInANewJvm {

        @Autowired
        private DataSource dataSource;

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        public void execute_one_select_after_an_assumption() {
            Assume.assumeTrue("Database not available", false);
            executeSelect(dataSource);
        }

    }

    @Test public void
    should_report_a_test_aborted_on_the_first_line_of_its_test_method_executed_in_a_new_jvm_as_skipped() {

        // GIVEN
        Class<?> testClass = AssumptionFailingOnTheFirstLineOfATestMethodExecutedInANewJvm.class;

        // WHEN
        JUnit4TestsResult testsResult = JUnit4TestsResult.run(testClass);

        // THEN
        String errorReport = testsResult.getErrorReport();

        assertThat(testsResult.getNumberOfFailures())
                .as("Nothing was measured: no select statement should be reported as missing.%s", errorReport)
                .isZero();

        assertThat(testsResult.getNumberOfAssumptionFailures())
                .as("The new JVM should report the assumption failure: the test should stay skipped.%s"
                  , errorReport)
                .isOne();

        assertThat(testsResult.getAssumptionFailureReport())
                .contains("Database not available");

    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(classes = SpringConfig.class)
    public static class AssumptionFailingOnTheFirstLineOfATestMethodExpectingAnException {

        @Autowired
        private DataSource dataSource;

        @ExpectSelect(1)
        @Test(expected = Exception.class)
        public void execute_one_select_after_an_assumption() throws Exception {
            Assume.assumeTrue("Database not available", false);
            executeSelect(dataSource);
        }

    }

    // JUnit 4.12 reports the test as skipped, even if the failing assumption is an instance of the expected exception
    @Test public void
    should_report_a_test_expecting_an_exception_and_aborted_on_the_first_line_of_its_test_method_as_skipped() {

        // GIVEN
        Class<?> testClass = AssumptionFailingOnTheFirstLineOfATestMethodExpectingAnException.class;

        // WHEN
        JUnit4TestsResult testsResult = JUnit4TestsResult.run(testClass);

        // THEN
        String errorReport = testsResult.getErrorReport();

        assertThat(testsResult.getNumberOfFailures())
                .as("Nothing was measured: no select statement should be reported as missing.%s", errorReport)
                .isZero();

        assertThat(testsResult.getNumberOfAssumptionFailures())
                .as("The test should stay skipped.%s", errorReport)
                .isOne();

    }

    // JUnit 4.12 does not count the assumption failures in its Result
    private static class JUnit4TestsResult {

        private final List<Failure> failures;

        private final List<Failure> assumptionFailures;

        private JUnit4TestsResult(List<Failure> failures, List<Failure> assumptionFailures) {
            this.failures = failures;
            this.assumptionFailures = assumptionFailures;
        }

        static JUnit4TestsResult run(Class<?> testClass) {
            JUnitCore jUnitCore = new JUnitCore();
            AssumptionFailureListener assumptionFailureListener = new AssumptionFailureListener();
            jUnitCore.addListener(assumptionFailureListener);
            Result result = jUnitCore.run(testClass);
            return new JUnit4TestsResult(result.getFailures(), assumptionFailureListener.assumptionFailures);
        }

        int getNumberOfFailures() {
            return failures.size();
        }

        int getNumberOfAssumptionFailures() {
            return assumptionFailures.size();
        }

        String getErrorReport() {
            return System.lineSeparator() + new PrintableResult(failures)
                 + System.lineSeparator() + "Assumption failures: " + getAssumptionFailureReport();
        }

        String getAssumptionFailureReport() {
            StringBuilder report = new StringBuilder();
            for (Failure assumptionFailure : assumptionFailures) {
                report.append(System.lineSeparator()).append(assumptionFailure.getTrace());
            }
            return report.toString();
        }

    }

    private static class AssumptionFailureListener extends RunListener {

        private final List<Failure> assumptionFailures = new CopyOnWriteArrayList<>();

        @Override
        public void testAssumptionFailure(Failure failure) {
            assumptionFailures.add(failure);
        }

    }

}
