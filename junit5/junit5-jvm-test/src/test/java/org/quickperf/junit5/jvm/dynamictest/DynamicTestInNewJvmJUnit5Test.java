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
package org.quickperf.junit5.jvm.dynamictest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.jvm.annotations.JvmOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * QuickPerf executes each dynamic test of a test factory having an annotation that needs a specific JVM
 * (<code>@HeapSize</code>, <code>@ExpectMaxHeapAllocation</code>...) in its own JVM. This JVM executes the test factory
 * method again, and then only this dynamic test, once. QuickPerf has to report the measures and the failures of this
 * execution for this dynamic test.
 * <p>
 * Some dynamic tests of the nested test classes fail on purpose with a {@link JvmExecutionReport}, to tell where they
 * were executed. Others fail if their JVM executes them more than once.
 * <p>
 * This module uses JUnit 5.6.2. See also the dynamic tests of the JUnit 5.14.4 test module.
 */
public class DynamicTestInNewJvmJUnit5Test {

    private static boolean executingInTheSpecificJvm() {
        return SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
    }

    @QuickPerfTest
    static class DynamicTestsFailingIfExecutedAgainInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return IntStream.rangeClosed(1, 3)
                            .mapToObj(number -> dynamicTest("dynamic test " + number, () ->
                                    assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne()));
        }

    }

    @Test public void
    should_execute_each_dynamic_test_once_in_its_jvm() {

        // GIVEN
        Class<?> testClass = DynamicTestsFailingIfExecutedAgainInTheSameJvm.class;
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
    static class DynamicTestsInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public List<DynamicTest> dynamic_tests() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(dynamicTest("apple", () -> { throw reportExecutionOf("apple"); }));
            dynamicTests.add(dynamicTest("banana", () -> { throw reportExecutionOf("banana"); }));
            dynamicTests.add(dynamicTest("cherry", () -> { throw reportExecutionOf("cherry"); }));
            return dynamicTests;
        }

    }

    @Test public void
    should_execute_each_dynamic_test_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = DynamicTestsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("apple", "banana", "cherry");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class DynamicContainersInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicContainer> dynamic_containers() {
            return Stream.of("red", "green")
                         .map(color -> dynamicContainer(color, Stream.of(color + " apple", color + " pear")
                                 .map(fruit -> dynamicTest(fruit, () -> { throw reportExecutionOf(fruit); }))));
        }

    }

    @Test public void
    should_execute_each_dynamic_test_of_dynamic_containers_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = DynamicContainersInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(4);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("red apple", "red pear", "green apple", "green pear");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class NestedTestClassHavingATestFactoryInASpecificJvm {

        @Nested
        class NestedTestClass {

            @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
            @TestFactory
            public Stream<DynamicTest> dynamic_tests() {
                return Stream.of("nested apple", "nested banana")
                             .map(fruit -> dynamicTest(fruit, () -> { throw reportExecutionOf(fruit); }));
            }

        }

    }

    @Test public void
    should_execute_each_dynamic_test_of_a_test_factory_of_a_nested_test_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = NestedTestClassHavingATestFactoryInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("nested apple", "nested banana");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class DynamicTestsAllocatingAccordingToTheirNumber {

        @ExpectMaxHeapAllocation(value = 2.5, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return IntStream.rangeClosed(1, 3)
                            .mapToObj(allocationInMegaBytes -> dynamicTest("allocating " + allocationInMegaBytes + " MB", () -> {
                                byte[] allocatedBytes = new byte[allocationInMegaBytes * 1024 * 1024];
                            }));
        }

    }

    @Test public void
    should_report_the_heap_allocation_of_each_dynamic_test_for_this_dynamic_test() {

        // GIVEN
        Class<?> testClass = DynamicTestsAllocatingAccordingToTheirNumber.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the dynamic test allocating 3 MB should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(2);

        assertThat(errorReport).contains("allocating 3 MB")
                               .contains("Expected heap allocation (test method thread) to be less than 2.50 Mega bytes but is 3.")
                               .doesNotContain("allocating 1 MB")
                               .doesNotContain("allocating 2 MB");

    }

    @QuickPerfTest
    static class DynamicTestAllocating440Bytes {

        @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocationJUnit5
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("array list with size 100", () -> {
                ArrayList<Object> data = new ArrayList<>(100);
            });
        }

    }

    @Test public void
    should_measure_the_heap_allocation_of_the_dynamic_test_only() {

        // GIVEN
        Class<?> testClass = DynamicTestAllocating440Bytes.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expected heap allocation (test method thread) to be less than 439 bytes but is 440 bytes");

    }

    @QuickPerfTest
    static class DynamicTestNotAllocating {

        @ExpectNoHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("not allocating", () -> { });
        }

    }

    @Test public void
    should_not_fail_a_dynamic_test_not_allocating_and_annotated_with_expect_no_heap_allocation() {

        // GIVEN
        Class<?> testClass = DynamicTestNotAllocating.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

    }

    @QuickPerfTest
    static class DynamicTestNotAllocatingWithQuickPerfRegisteredTwice {

        // Registered in addition to the extension of @QuickPerfTest. The specific JVM executes it inside the recording
        // of the other one.
        @RegisterExtension
        static QuickPerfTestExtension quickPerfTestExtension = new QuickPerfTestExtension();

        @ExpectNoHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("not allocating", () -> { });
        }

    }

    @Test public void
    should_measure_a_dynamic_test_once_in_its_jvm_with_quickperf_registered_twice() {

        // GIVEN
        Class<?> testClass = DynamicTestNotAllocatingWithQuickPerfRegisteredTwice.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

    }

    @QuickPerfTest
    static class TestFactoryWhoseFirstDynamicTestFailsInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public List<DynamicTest> dynamic_tests() {
            return Arrays.asList(dynamicTest("failing dynamic test", () -> { throw new AssertionError("Failure of the first dynamic test"); })
                               , dynamicTest("passing dynamic test", () -> { }));
        }

    }

    @Test public void
    should_report_the_failure_of_a_dynamic_test_for_this_dynamic_test_only() {

        // GIVEN
        Class<?> testClass = TestFactoryWhoseFirstDynamicTestFailsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the first dynamic test should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

        assertThat(errorReport).contains("failing dynamic test")
                               .contains("Failure of the first dynamic test")
                               .doesNotContain("passing dynamic test");

    }

    @QuickPerfTest
    static class TestFactoryProvidingFewerDynamicTestsInTheSpecificJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            int numberOfDynamicTests = executingInTheSpecificJvm() ? 2 : 3;
            return IntStream.rangeClosed(1, numberOfDynamicTests)
                            .mapToObj(number -> dynamicTest("dynamic test " + number, () -> { }));
        }

    }

    @Test public void
    should_fail_the_dynamic_test_that_does_not_exist_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = TestFactoryProvidingFewerDynamicTestsInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the third dynamic test should fail, because it does not exist in the specific JVM.%s"
                  , errorReport)
                .isOne();

        assertThat(errorReport).contains("JUnit started 0 tests instead of 1 in the specific JVM")
                               .contains("[dynamic-test:#3]")
                               .contains("The test factory method provides other dynamic tests in the specific JVM")
                               .contains("If the dynamic tests come from the methods of a class, sort these methods, for example by Method.toString()")
                               .contains("Class.getDeclaredMethods() and Class.getMethods() return them in an order that changes from one JVM to another")
                               .doesNotContain("UnexpectedQuickPerfException");

    }

    @QuickPerfTest
    static class TestFactoryProvidingItsDynamicTestsInAnotherOrderInTheSpecificJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public List<DynamicTest> dynamic_tests() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(dynamicTest("allocating nothing", () -> { }));
            dynamicTests.add(dynamicTest("allocating 3 MB", () -> {
                byte[] allocatedBytes = new byte[3 * 1024 * 1024];
                throw new AssertionError("Failure of the dynamic test allocating 3 MB");
            }));
            if (executingInTheSpecificJvm()) {
                Collections.reverse(dynamicTests);
            }
            return dynamicTests;
        }

    }

    @Test public void
    should_fail_a_dynamic_test_having_another_display_name_in_the_specific_jvm_without_reporting_its_measures_and_failures() {

        // GIVEN
        Class<?> testClass = TestFactoryProvidingItsDynamicTestsInAnotherOrderInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail, because the specific JVM executes the other one.%s", errorReport)
                .isEqualTo(2);

        assertThat(errorReport).contains("[dynamic-test:#1], the display name of the dynamic test is \"allocating 3 MB\", and not \"allocating nothing\"")
                               .contains("[dynamic-test:#2], the display name of the dynamic test is \"allocating nothing\", and not \"allocating 3 MB\"")
                               .contains("The test factory method provides other dynamic tests, or the same ones in another order, in the specific JVM")
                               .contains("If the dynamic tests come from the methods of a class, sort these methods, for example by Method.toString()")
                               .contains("Class.getDeclaredMethods() and Class.getMethods() return them in an order that changes from one JVM to another")
                               .contains("The display name of the dynamic test changes from one JVM to another")
                               .contains("The display name of the dynamic test depends on a system property of the test JVM")
                               .doesNotContain("Expected heap allocation")
                               .doesNotContain("Failure of the dynamic test allocating 3 MB");

    }

    @QuickPerfTest
    static class AssumptionFailingInADynamicTestInTheSpecificJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("dynamic test", () ->
                    assumeFalse(executingInTheSpecificJvm(), "Assumption failing in a dynamic test of the specific JVM"));
        }

    }

    @Test public void
    should_report_a_dynamic_test_aborted_in_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInADynamicTestInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The dynamic test should be aborted, as in the specific JVM.")
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing in a dynamic test of the specific JVM");

    }

    @QuickPerfTest
    static class TestFactoryProvidingDynamicTestsFromAStateSetByABeforeEachMethod {

        private List<String> fruits;

        @BeforeEach
        public void before_each() {
            fruits = Arrays.asList("apple", "banana");
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return fruits.stream()
                         .map(fruit -> dynamicTest(fruit, () -> {
                             assertThat(fruits).as("Fruits set by the @BeforeEach method").contains(fruit);
                             throw reportExecutionOf(fruit);
                         }));
        }

    }

    @Test public void
    should_execute_the_before_each_methods_of_a_test_factory_in_the_test_jvm_and_in_the_specific_jvms() {

        // GIVEN
        Class<?> testClass = TestFactoryProvidingDynamicTestsFromAStateSetByABeforeEachMethod.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("apple", "banana");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm {

        private static final AtomicInteger BEFORE_EACH_EXECUTIONS_IN_THE_TEST_JVM = new AtomicInteger();

        private static final AtomicInteger AFTER_EACH_EXECUTIONS_IN_THE_TEST_JVM = new AtomicInteger();

        @BeforeEach
        public void before_each() {
            if (!executingInTheSpecificJvm()) {
                BEFORE_EACH_EXECUTIONS_IN_THE_TEST_JVM.incrementAndGet();
            }
        }

        // Has to release in the test JVM what the @BeforeEach method acquired there, a lock or a file for example
        @AfterEach
        public void after_each() {
            if (!executingInTheSpecificJvm()) {
                AFTER_EACH_EXECUTIONS_IN_THE_TEST_JVM.incrementAndGet();
            }
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return Stream.of("apple", "banana")
                         .map(fruit -> dynamicTest(fruit, () -> { }));
        }

    }

    @Test public void
    should_execute_the_before_each_and_after_each_methods_of_a_test_factory_once_in_the_test_jvm() {

        // GIVEN
        TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm.BEFORE_EACH_EXECUTIONS_IN_THE_TEST_JVM.set(0);
        TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm.AFTER_EACH_EXECUTIONS_IN_THE_TEST_JVM.set(0);
        Class<?> testClass = TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(2);

        assertThat(TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm.BEFORE_EACH_EXECUTIONS_IN_THE_TEST_JVM.get())
                .as("@BeforeEach executions in the test JVM")
                .isOne();

        assertThat(TestFactoryCountingItsBeforeEachAndAfterEachExecutionsInTheTestJvm.AFTER_EACH_EXECUTIONS_IN_THE_TEST_JVM.get())
                .as("@AfterEach executions in the test JVM")
                .isOne();

    }

    @QuickPerfTest
    static class TestFactoryHavingAnAfterEachMethodCheckingWhatTheDynamicTestsDid {

        private final List<String> unreleasedResources = new ArrayList<>();

        // The test JVM does not execute the dynamic tests: this check only fails in the specific JVM of the dynamic test
        // not releasing its resource
        @AfterEach
        public void after_each() {
            assertThat(unreleasedResources).as("Resources not released by the dynamic tests").isEmpty();
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public List<DynamicTest> dynamic_tests() {
            return Arrays.asList(dynamicTest("dynamic test not releasing its resource", () -> unreleasedResources.add("resource"))
                               , dynamicTest("dynamic test releasing its resource", () -> { }));
        }

    }

    @Test public void
    should_report_a_failure_of_the_test_factory_caused_by_a_dynamic_test_in_the_specific_jvm_on_this_dynamic_test() {

        // GIVEN
        Class<?> testClass = TestFactoryHavingAnAfterEachMethodCheckingWhatTheDynamicTestsDid.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the dynamic test not releasing its resource should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

        assertThat(errorReport).contains("dynamic test not releasing its resource")
                               .contains("JUnit reported a failure of dynamic_tests() after the dynamic test in the specific JVM")
                               .contains("Resources not released by the dynamic tests");

    }

    @QuickPerfTest
    static class DynamicContainerFailingAfterItsFirstDynamicTestInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public DynamicContainer dynamic_container() {
            Stream<DynamicTest> dynamicTests = Stream.of("first dynamic test", "second dynamic test")
                                                     .map(name -> {
                                                         if (name.startsWith("second")) {
                                                             throw new IllegalStateException("Failure of the dynamic container after its first dynamic test");
                                                         }
                                                         return dynamicTest(name, () -> { });
                                                     });
            return dynamicContainer("container", dynamicTests);
        }

    }

    @Test public void
    should_report_a_failure_of_a_dynamic_container_after_a_dynamic_test_in_the_specific_jvm_on_this_dynamic_test() {

        // GIVEN
        Class<?> testClass = DynamicContainerFailingAfterItsFirstDynamicTestInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The dynamic container should fail in the test JVM, and the first dynamic test in its JVM.%s"
                  , errorReport)
                .isEqualTo(2);

        assertThat(errorReport).contains("JUnit reported a failure of container after the dynamic test in the specific JVM")
                               .contains("Failure of the dynamic container after its first dynamic test");

    }

    @QuickPerfTest
    static class TestMethodAlsoAnnotatedWithTestFactoryInASpecificJvm {

        // JUnit executes a method returning void, and annotated with @Test and @TestFactory, as a test method
        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        @TestFactory
        public void test_method() {
            assertThat(executingInTheSpecificJvm()).as("Test method executed in the specific JVM").isTrue();
        }

    }

    @Test public void
    should_execute_a_test_method_also_annotated_with_test_factory_as_a_test_method() {

        // GIVEN
        Class<?> testClass = TestMethodAlsoAnnotatedWithTestFactoryInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

    }

    @QuickPerfTest
    static class AssumptionFailingInADynamicTestAllocatingInTheSpecificJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("dynamic test allocating 3 MB", () -> {
                byte[] allocatedBytes = new byte[3 * 1024 * 1024];
                assumeFalse(executingInTheSpecificJvm(), "Assumption failing in a dynamic test of the specific JVM");
            });
        }

    }

    @Test public void
    should_not_report_the_performance_issue_of_a_dynamic_test_aborted_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInADynamicTestAllocatingInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The abort cut the measurement short: the dynamic test should be aborted, as when an assumption"
                  + " aborts a dynamic test that does not need a specific JVM.%s", errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The dynamic test should be aborted, as in the specific JVM.")
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing in a dynamic test of the specific JVM");

        assertThat(errorReport).doesNotContain("Expected heap allocation");

    }

}
