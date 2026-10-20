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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.ForkJoinPoolBarrier;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * With the parallel execution of JUnit, the dynamic tests of a test factory can be executed concurrently. QuickPerf then
 * executes them concurrently, each one in its own JVM: their working folders and their measures must not mix.
 */
public class ConcurrentDynamicTestsInNewJvmJUnit5Test {

    private static final int NUMBER_OF_DYNAMIC_TESTS = 3;

    private static final Map<String, String> PARALLEL_EXECUTION = new HashMap<>();

    static {
        PARALLEL_EXECUTION.put("junit.jupiter.execution.parallel.enabled", "true");
        PARALLEL_EXECUTION.put("junit.jupiter.execution.parallel.mode.default", "concurrent");
        PARALLEL_EXECUTION.put("junit.jupiter.execution.parallel.config.strategy", "fixed");
        PARALLEL_EXECUTION.put("junit.jupiter.execution.parallel.config.fixed.parallelism", "6");
    }

    // Registered before QuickPerf. In the test JVM, the dynamic tests of a test factory wait for each other before
    // QuickPerf executes them in their JVMs: these JVMs are executed concurrently.
    public static class DynamicTestsStartingTogether implements InvocationInterceptor {

        private static final ConcurrentMap<String, CyclicBarrier> BARRIER_BY_TEST_FACTORY = new ConcurrentHashMap<>();

        @Override
        public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
            if (!SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
                String testFactoryUniqueId = extensionContext.getParent().get().getUniqueId();
                CyclicBarrier barrier = BARRIER_BY_TEST_FACTORY.computeIfAbsent(testFactoryUniqueId
                                                                    , uniqueId -> new CyclicBarrier(NUMBER_OF_DYNAMIC_TESTS));
                ForkJoinPoolBarrier.await(barrier);
            }
            invocation.proceed();
        }

    }

    @ExtendWith(DynamicTestsStartingTogether.class)
    @QuickPerfTest
    static class ConcurrentDynamicTestsInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return IntStream.rangeClosed(1, NUMBER_OF_DYNAMIC_TESTS)
                            .mapToObj(number -> "dynamic test " + number)
                            .map(name -> dynamicTest(name, () -> { throw reportExecutionOf(name); }));
        }

    }

    @Test public void
    should_execute_each_dynamic_test_in_its_own_jvm_when_the_dynamic_tests_are_executed_concurrently() {

        // GIVEN
        Class<?> testClass = ConcurrentDynamicTestsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass, PARALLEL_EXECUTION);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(NUMBER_OF_DYNAMIC_TESTS);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("dynamic test 1", "dynamic test 2", "dynamic test 3");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @ExtendWith(DynamicTestsStartingTogether.class)
    @QuickPerfTest
    static class ConcurrentDynamicTestsAllocatingAccordingToTheirNumber {

        @ExpectMaxHeapAllocation(value = 2.5, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return IntStream.rangeClosed(1, NUMBER_OF_DYNAMIC_TESTS)
                            .mapToObj(allocationInMegaBytes -> dynamicTest("allocating " + allocationInMegaBytes + " MB", () -> {
                                byte[] allocatedBytes = new byte[allocationInMegaBytes * 1024 * 1024];
                            }));
        }

    }

    @Test public void
    should_report_the_heap_allocation_of_each_dynamic_test_for_this_dynamic_test_when_the_dynamic_tests_are_executed_concurrently() {

        // GIVEN
        Class<?> testClass = ConcurrentDynamicTestsAllocatingAccordingToTheirNumber.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass, PARALLEL_EXECUTION);

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

}
