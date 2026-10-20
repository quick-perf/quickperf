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
package org.quickperf.junit5.dynamictest;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.junit5.QuickPerfTestExtension;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * JUnit 5.8 to 5.14 intercept a dynamic test with
 * <code>InvocationInterceptor.interceptDynamicTest(Invocation, DynamicTestInvocationContext, ExtensionContext)</code>,
 * whose default implementation calls
 * <code>InvocationInterceptor.interceptDynamicTest(Invocation, ExtensionContext)</code>, the method that QuickPerf
 * overrides. The other JUnit 5 test modules use JUnit 5.6.2, which directly calls this method.
 */
public class DynamicTestInvocationContextInNewJvmJUnit5Test {

    @QuickPerfTest
    static class DynamicTestsExecutedInTheTestJvm {

        @ExpectMaxExecutionTime(milliSeconds = 100)
        @TestFactory
        public Stream<DynamicTest> dynamic_tests() {
            return Stream.of(dynamicTest("dynamic test during about one second", () -> Thread.sleep(1_000L))
                           , dynamicTest("dynamic test without sleep", () -> { }));
        }

    }

    @Test public void
    should_check_the_annotations_of_a_test_factory_on_each_dynamic_test_executed_in_the_test_jvm() {

        // GIVEN
        Class<?> testClass = DynamicTestsExecutedInTheTestJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the dynamic test during about one second should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

        assertThat(errorReport).contains("dynamic test during about one second")
                               .doesNotContain("dynamic test without sleep");

    }

    @QuickPerfTest
    static class DynamicTestExecutedInTheTestJvmWithQuickPerfRegisteredTwice {

        // Registered in addition to the extension of @QuickPerfTest. The other one executes it when it executes the
        // invocation.
        @RegisterExtension
        static QuickPerfTestExtension quickPerfTestExtension = new QuickPerfTestExtension();

        @ExpectMaxExecutionTime(milliSeconds = 10)
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("dynamic test during about 200 ms", () -> Thread.sleep(200L));
        }

    }

    @Test public void
    should_report_the_performance_issue_of_a_dynamic_test_once_with_quickperf_registered_twice() {

        // GIVEN
        Class<?> testClass = DynamicTestExecutedInTheTestJvmWithQuickPerfRegisteredTwice.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isOne();

        assertThat(errorReport).contains("a performance-related property is not respected")
                               .as("Report of the other instance, nested as a functional failure")
                               .doesNotContain("Performance-related and functional properties not respected");

    }

    @QuickPerfTest
    static class DynamicTestAllocating440Bytes {

        @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @TestFactory
        public DynamicTest dynamic_test() {
            return dynamicTest("array list with size 100", () -> {
                // java.util.ArrayList: 24 bytes + Object[]: 16 + 100 x 4 = 416 bytes -> 440 bytes
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

}
