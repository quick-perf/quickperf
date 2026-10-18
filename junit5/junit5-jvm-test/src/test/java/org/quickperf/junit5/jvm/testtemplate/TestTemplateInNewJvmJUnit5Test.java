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
package org.quickperf.junit5.jvm.testtemplate;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * QuickPerf executes each invocation of a test template (<code>@RepeatedTest</code>, <code>@ParameterizedTest</code>...)
 * having an annotation that needs a specific JVM (<code>@HeapSize</code>, <code>@ExpectMaxHeapAllocation</code>...) in
 * its own JVM. This JVM has to execute this invocation once, with its arguments, and QuickPerf has to report the
 * measures and the failures of this execution for this invocation.
 * <p>
 * Some test methods of the nested test classes fail on purpose with a {@link JvmExecutionReport}, to tell where they
 * were executed. Others fail if their JVM executes them more than once. QuickPerf fails a test that JUnit does not
 * execute in the specific JVM.
 */
public class TestTemplateInNewJvmJUnit5Test {

    @QuickPerfTest
    static class RepeatedTestFailingIfExecutedAgainInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @RepeatedTest(3)
        public void repeated_test() {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne();
        }

    }

    @Test public void
    should_execute_each_repetition_once_in_its_jvm() {

        // GIVEN
        Class<?> testClass = RepeatedTestFailingIfExecutedAgainInTheSameJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(3);

    }

    @QuickPerfTest
    static class RepeatedTestHavingARepetitionInfoParameterInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @RepeatedTest(3)
        public void repeated_test(RepetitionInfo repetitionInfo) {
            throw reportExecutionOf("repetition " + repetitionInfo.getCurrentRepetition()
                                  + " of " + repetitionInfo.getTotalRepetitions());
        }

    }

    @Test public void
    should_execute_each_repetition_of_a_repeated_test_having_a_repetition_info_parameter_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = RepeatedTestHavingARepetitionInfoParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each repetition should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("repetition 1 of 3", "repetition 2 of 3", "repetition 3 of 3");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class RepeatedTestAllocatingMoreAtEachExecutionInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        // The first execution in a JVM allocates 1 MB, the second one 2 MB...
        @ExpectMaxHeapAllocation(value = 1.5, unit = AllocationUnit.MEGA_BYTE)
        @RepeatedTest(3)
        public void repeated_test() {
            int executionNumber = EXECUTIONS_IN_THIS_JVM.incrementAndGet();
            byte[] allocatedBytes = new byte[executionNumber * 1024 * 1024];
        }

    }

    @Test public void
    should_measure_the_heap_allocation_of_each_repetition_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = RepeatedTestAllocatingMoreAtEachExecutionInTheSameJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each repetition should be executed alone in its JVM, and allocate 1 MB.%s", errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(3);

    }

    @QuickPerfTest
    static class ParameterizedTestFailingIfExecutedAgainInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        public void parameterized_test(int value) {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne();
        }

    }

    @Test public void
    should_execute_each_invocation_of_a_parameterized_test_once_in_its_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedTestFailingIfExecutedAgainInTheSameJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(3);

    }

    @QuickPerfTest
    static class ParameterizedTestInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest(name = "value {0}")
        @ValueSource(ints = {1, 2, 3})
        public void parameterized_test(int value) {
            throw reportExecutionOf("value " + value);
        }

    }

    @Test public void
    should_execute_each_invocation_of_a_parameterized_test_in_its_own_jvm_with_its_argument() {

        // GIVEN
        Class<?> testClass = ParameterizedTestInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("value 1", "value 2", "value 3");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class ParameterizedTestAllocatingAccordingToItsArgument {

        @ExpectMaxHeapAllocation(value = 2.5, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest(name = "allocating {0} MB")
        @ValueSource(ints = {1, 2, 3})
        public void parameterized_test(int allocationInMegaBytes) {
            byte[] allocatedBytes = new byte[allocationInMegaBytes * 1024 * 1024];
        }

    }

    @Test public void
    should_report_the_heap_allocation_of_each_invocation_of_a_parameterized_test_for_this_invocation() {

        // GIVEN
        Class<?> testClass = ParameterizedTestAllocatingAccordingToItsArgument.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the invocation allocating 3 MB should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(2);

        assertThat(errorReport).contains("allocating 3 MB")
                               .contains("Expected heap allocation (test method thread) to be less than 2.50 Mega bytes but is 3.")
                               .doesNotContain("allocating 1 MB")
                               .doesNotContain("allocating 2 MB");

    }

    @QuickPerfTest
    static class ParameterizedTestHavingSeveralArgumentsAndATestInfoParameterInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest(name = "{0} x {1}")
        @CsvSource({"apple, 1", "banana, 2"})
        public void parameterized_test(String fruit, int quantity, TestInfo testInfo) {
            String executedTest = fruit + " x " + quantity;
            assertThat(testInfo.getDisplayName()).isEqualTo(executedTest);
            throw reportExecutionOf(executedTest);
        }

    }

    @Test public void
    should_execute_each_invocation_of_a_parameterized_test_having_several_arguments_and_a_test_info_parameter_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedTestHavingSeveralArgumentsAndATestInfoParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("apple x 1", "banana x 2");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class ParameterizedTestHavingAnArrayParameterInASpecificJvm {

        static Stream<Arguments> fruits() {
            return Stream.of(arguments((Object) new String[] {"apple", "banana"})
                           , arguments((Object) new String[] {"cherry"}));
        }

        // The JUnit unique id of the test contains the array type, encoded: parameterized_test(%5BLjava.lang.String;)
        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest(name = "{0}")
        @MethodSource("fruits")
        public void parameterized_test(String[] fruits) {
            throw reportExecutionOf(Arrays.toString(fruits));
        }

    }

    @Test public void
    should_execute_each_invocation_of_a_parameterized_test_having_an_array_parameter_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedTestHavingAnArrayParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("[apple, banana]", "[cherry]");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class NestedTestClassHavingAParameterizedTestInASpecificJvm {

        @Nested
        class NestedTestClass {

            @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
            @ParameterizedTest(name = "nested value {0}")
            @ValueSource(ints = {1, 2})
            public void parameterized_test(int value) {
                throw reportExecutionOf("nested value " + value);
            }

        }

    }

    @Test public void
    should_execute_each_invocation_of_a_parameterized_test_of_a_nested_test_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = NestedTestClassHavingAParameterizedTestInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("nested value 1", "nested value 2");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

}
