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

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationJUnit5TestTemplateTest {

    @QuickPerfTest
    public static class ClassWithMethodAnnotatedWithExpectMaxHeapAllocation {

        @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
        // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @RepeatedTest(2)
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
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(2);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expected heap allocation (test method thread) to be less than 439 bytes but is 440 bytes");

    }

}
