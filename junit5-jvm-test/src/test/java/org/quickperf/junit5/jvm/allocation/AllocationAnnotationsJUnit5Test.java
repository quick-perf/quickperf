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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.junit5.jvm.allocation;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationAnnotationsJUnit5Test {

    @QuickPerfTest
    public static class ClassWithMethodAnnotatedWithExpectMaxHeapAllocation {

        @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void array_list_with_size_100_should_allocate_440_bytes() {
            ArrayList<Object> data = new ArrayList<>(100);
        }

    }

    @Test public void
    test_should_fail_if_allocation_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = ClassWithMethodAnnotatedWithExpectMaxHeapAllocation.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(1);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expected allocation to be less than 439.0 bytes but is 440.0 bytes");

    }

    @QuickPerfTest()
    public static class ClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation {

        @ExpectNoHeapAllocation
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void method_allocating() {
            ArrayList<Object> data = new ArrayList<>(100);
        }

    }

    @Test
    public void test_should_fail_if_test_method_annotated_with_no_allocation_allocates() {

        // GIVEN
        Class<?> testClass = ClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(1);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expected allocation to be 0 but is 440.0 bytes");

    }

    @QuickPerfTest
    public static class ClassWithAMethodNotAllocatingAndAnnotatedWithExpectNoHeapAllocation {

        @ExpectNoHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void method_without_allocation() {
        }

    }

    @Test
    public void test_should_not_fail_with_a_method_without_allocation_and_annotation_with_no_allocation() {

        // GIVEN
        Class<?> testClass = ClassWithAMethodNotAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(0);

    }

}
