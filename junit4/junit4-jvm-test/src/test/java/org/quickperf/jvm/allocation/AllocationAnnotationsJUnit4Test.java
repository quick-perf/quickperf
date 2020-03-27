/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.jvm.allocation;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class AllocationAnnotationsJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
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

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(
                "Expected allocation (test method thread) to be less than 439.0 bytes but is 440.0 bytes.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation {

        @ExpectNoHeapAllocation
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void method_allocating() {
            ArrayList<Object> data = new ArrayList<>(100);
        }

    }

    @Test public void
    test_should_fail_if_test_method_annotated_with_no_allocation_allocates() {

        // GIVEN
        Class<?> testClass = ClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                      .contains("Expected allocation (test method thread) to be 0 but is 440.0 bytes.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithAMethodNotAllocatingAndAnnotatedWithExpectNoHeapAllocation {

        @ExpectNoHeapAllocation
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void method_without_allocation() {
        }

    }

    @Test public void
    test_should_not_fail_with_a_method_without_allocation_and_annotation_with_no_allocation() {

        // GIVEN
        Class<?> testClass = ClassWithAMethodNotAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
