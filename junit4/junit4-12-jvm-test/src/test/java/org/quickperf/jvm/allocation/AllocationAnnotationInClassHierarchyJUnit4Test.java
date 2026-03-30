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
package org.quickperf.jvm.allocation;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class AllocationAnnotationInClassHierarchyJUnit4Test {

    public abstract static class AbstractClassWithExpectMaxHeapAllocation {

        @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void array_list_with_size_100() {
            ArrayList<Object> data = new ArrayList<>(100);
        }

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConcreteClassExtendingAbstract extends AbstractClassWithExpectMaxHeapAllocation {

    }

    @Test public void
    concrete_class_inheriting_allocation_annotation_should_measure_allocation() {

        // GIVEN
        Class<?> testClass = ConcreteClassExtendingAbstract.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(
                "Expected heap allocation (test method thread) to be less than 439 bytes but is 440 bytes");

    }

}
