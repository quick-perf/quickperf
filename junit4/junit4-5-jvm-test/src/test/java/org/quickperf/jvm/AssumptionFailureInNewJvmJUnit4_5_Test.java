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
package org.quickperf.jvm;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.quickperf.SystemProperties;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class AssumptionFailureInNewJvmJUnit4_5_Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInATestMethodExecutedInNewJvm {

        // Executes the test method in a new JVM. The assumption failure allocates: QuickPerf would report a
        // performance issue if it evaluated the recording.
        @ExpectNoHeapAllocation
        @Test
        public void test_method_with_a_failing_assumption() {
            Assume.assumeTrue(false);
        }

    }

    @Test public void
    should_report_the_assumption_failure_of_a_test_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInATestMethodExecutedInNewJvm.class;
        JUnitCore jUnitCore = new JUnitCore();
        AssumptionFailureRecorder assumptionFailureRecorder = new AssumptionFailureRecorder();
        jUnitCore.addListener(assumptionFailureRecorder);

        // WHEN
        Result result = jUnitCore.run(testClass);

        // THEN
        String errorReport = buildErrorReport(result, assumptionFailureRecorder);

        assertThat(result.getFailureCount())
                .as(errorReport)
                .isZero();

        assertThat(assumptionFailureRecorder.getAssumptionFailures())
                .as(errorReport)
                .hasSize(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInABeforeClassMethodOfTheNewJvm {

        @BeforeClass
        public static void assume_in_a_before_class_method_of_the_new_jvm() {
            boolean executingInTheNewJvm = SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
            Assume.assumeTrue(!executingInTheNewJvm);
        }

        // Executes the test method in a new JVM. Without recording, QuickPerf would fail to evaluate the allocation.
        @ExpectNoHeapAllocation
        @Test
        public void test_method() {
        }

    }

    // JUnit 4.5 reports the test class as ignored, without the assumption failure, when an assumption fails in a
    // @BeforeClass method
    @Test public void
    should_report_the_assumption_failure_of_a_before_class_method_of_the_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeClassMethodOfTheNewJvm.class;
        JUnitCore jUnitCore = new JUnitCore();
        AssumptionFailureRecorder assumptionFailureRecorder = new AssumptionFailureRecorder();
        jUnitCore.addListener(assumptionFailureRecorder);

        // WHEN
        Result result = jUnitCore.run(testClass);

        // THEN
        String errorReport = buildErrorReport(result, assumptionFailureRecorder);

        assertThat(result.getFailureCount())
                .as(errorReport)
                .isZero();

        assertThat(assumptionFailureRecorder.getAssumptionFailures())
                .as(errorReport)
                .hasSize(1);

    }

    private static String buildErrorReport(Result result, AssumptionFailureRecorder assumptionFailureRecorder) {
        StringBuilder errorReport = new StringBuilder();
        for (Failure failure : result.getFailures()) {
            errorReport.append("Failure: ").append(failure.getTrace()).append(System.lineSeparator());
        }
        for (Failure assumptionFailure : assumptionFailureRecorder.getAssumptionFailures()) {
            errorReport.append("Assumption failure: ").append(assumptionFailure.getTrace())
                       .append(System.lineSeparator());
        }
        return errorReport.toString();
    }

    // Result does not count the assumption failures in JUnit 4.5
    private static class AssumptionFailureRecorder extends RunListener {

        private final List<Failure> assumptionFailures = new CopyOnWriteArrayList<>();

        @Override
        public void testAssumptionFailure(Failure failure) {
            assumptionFailures.add(failure);
        }

        List<Failure> getAssumptionFailures() {
            return assumptionFailures;
        }

    }

}
