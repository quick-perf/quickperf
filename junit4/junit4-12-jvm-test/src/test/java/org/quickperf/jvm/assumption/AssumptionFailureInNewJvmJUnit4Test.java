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
package org.quickperf.jvm.assumption;

import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

// In JUnit 4.12, an assumption failing on a value, as Assume.assumeTrue(boolean) without message, keeps a matcher that
// cannot be serialized. The new JVM has to send another abort to the test JVM.
public class AssumptionFailureInNewJvmJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionWithoutMessageFailingInATestMethodExecutedInNewJvm {

        // Executes the test method in a new JVM. The assumption failure allocates: QuickPerf would report a
        // performance issue if it evaluated the recording.
        @ExpectNoHeapAllocation
        @Test
        public void test_method_with_a_failing_assumption() {
            Assume.assumeTrue(false);
        }

    }

    @Test public void
    should_report_an_assumption_failure_without_message_of_a_test_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionWithoutMessageFailingInATestMethodExecutedInNewJvm.class;
        JUnitCore jUnitCore = new JUnitCore();
        TestOutcomeRecorder testOutcomeRecorder = new TestOutcomeRecorder();
        jUnitCore.addListener(testOutcomeRecorder);

        // WHEN
        Result result = jUnitCore.run(testClass);

        // THEN
        String errorReport = testOutcomeRecorder.buildErrorReport();

        assertThat(result.getFailureCount())
                .as(errorReport)
                .isZero();

        List<Failure> assumptionFailures = testOutcomeRecorder.getAssumptionFailures();
        assertThat(assumptionFailures)
                .as(errorReport)
                .hasSize(1);

        assertThat(assumptionFailures.get(0).getMessage())
                .as(errorReport)
                .contains("got: <false>, expected: is <true>");

        assertThat(testOutcomeRecorder.getPassedTests())
                .as(errorReport)
                .isEmpty();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInATestMethodExpectingAnExceptionExecutedInNewJvm {

        // Executes the test method in a new JVM. The assumption failure allocates: QuickPerf would report a
        // performance issue if it evaluated the recording.
        @ExpectNoHeapAllocation
        @Test(expected = Exception.class)
        public void test_method_with_a_failing_assumption() {
            Assume.assumeTrue(false);
        }

    }

    // JUnit 4.12 reports the test as skipped, even if the failing assumption is an instance of the expected exception
    @Test public void
    should_report_an_assumption_failure_of_a_test_method_expecting_an_exception_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInATestMethodExpectingAnExceptionExecutedInNewJvm.class;
        JUnitCore jUnitCore = new JUnitCore();
        TestOutcomeRecorder testOutcomeRecorder = new TestOutcomeRecorder();
        jUnitCore.addListener(testOutcomeRecorder);

        // WHEN
        Result result = jUnitCore.run(testClass);

        // THEN
        String errorReport = testOutcomeRecorder.buildErrorReport();

        assertThat(result.getFailureCount())
                .as(errorReport)
                .isZero();

        assertThat(testOutcomeRecorder.getAssumptionFailures())
                .as(errorReport)
                .hasSize(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionWithoutMessageFailingInAnAfterMethodOfATestExecutedInNewJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_allocating_three_mega_bytes() {
            byte[] allocatedBytes = new byte[3 * 1024 * 1024];
        }

        @After
        public void after() {
            Assume.assumeTrue(false);
        }

    }

    @Test public void
    should_report_the_performance_issue_and_the_assumption_failure_without_message_of_an_after_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionWithoutMessageFailingInAnAfterMethodOfATestExecutedInNewJvm.class;
        JUnitCore jUnitCore = new JUnitCore();
        TestOutcomeRecorder testOutcomeRecorder = new TestOutcomeRecorder();
        jUnitCore.addListener(testOutcomeRecorder);

        // WHEN
        Result result = jUnitCore.run(testClass);

        // THEN
        String errorReport = testOutcomeRecorder.buildErrorReport();

        assertThat(result.getFailureCount())
                .as(errorReport)
                .isOne();

        assertThat(result.getFailures().get(0).getMessage())
                .as(errorReport)
                .contains("Performance-related and functional properties not respected")
                .contains("Expected heap allocation (test method thread) to be less than 1.00 Mega bytes but is 3.")
                .contains("got: <false>, expected: is <true>");

    }

    // Result.getAssumptionFailureCount() does not exist before JUnit 4.13
    private static class TestOutcomeRecorder extends RunListener {

        private final List<Failure> failures = new CopyOnWriteArrayList<>();

        private final List<Failure> assumptionFailures = new CopyOnWriteArrayList<>();

        private final List<Description> testsNotPassed = new CopyOnWriteArrayList<>();

        private final List<Description> passedTests = new CopyOnWriteArrayList<>();

        @Override
        public void testFailure(Failure failure) {
            failures.add(failure);
            testsNotPassed.add(failure.getDescription());
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            assumptionFailures.add(failure);
            testsNotPassed.add(failure.getDescription());
        }

        @Override
        public void testFinished(Description description) {
            if (!testsNotPassed.contains(description)) {
                passedTests.add(description);
            }
        }

        List<Failure> getAssumptionFailures() {
            return assumptionFailures;
        }

        List<Description> getPassedTests() {
            return passedTests;
        }

        String buildErrorReport() {
            StringBuilder errorReport = new StringBuilder();
            for (Failure failure : failures) {
                errorReport.append("Failure: ").append(failure.getTrace()).append(System.lineSeparator());
            }
            for (Failure assumptionFailure : assumptionFailures) {
                errorReport.append("Assumption failure: ").append(assumptionFailure.getTrace())
                           .append(System.lineSeparator());
            }
            errorReport.append("Passed tests: ").append(passedTests);
            return errorReport.toString();
        }

    }

}
