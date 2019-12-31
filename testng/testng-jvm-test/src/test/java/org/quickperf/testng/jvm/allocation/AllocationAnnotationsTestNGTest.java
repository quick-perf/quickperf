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

package org.quickperf.testng.jvm.allocation;

import org.quickperf.testng.TestNGTests;
import org.quickperf.testng.TestNGTests.TestNGTestsResult;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationAnnotationsTestNGTest {

    @Test public void
    test_should_fail_if_allocation_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = TestNGClassWithMethodAnnotatedWithExpectMaxHeapAllocation.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isEqualTo(1);

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining(
                "Expected allocation to be less than 439.0 bytes but is 440.0 bytes");

    }

    @Test
    public void test_should_fail_if_test_method_annotated_with_no_allocation_allocates() {

        // GIVEN
        Class<?> testClass = TestNGClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isEqualTo(1);

        assertThat(testsResult.getThrowableOfFirstTest()).hasMessageContaining(
                "Expected allocation to be 0 but is 440.0 bytes");

    }

    @Test
    public void test_should_not_fail_with_a_method_without_allocation_and_annotated_with_no_allocation() {

        // GIVEN
        Class<?> testClass = TestNGClassWithAMethodNotAllocatingAndAnnotatedWithExpectNoHeapAllocation.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfPassedTest()).isEqualTo(1);

    }

}
