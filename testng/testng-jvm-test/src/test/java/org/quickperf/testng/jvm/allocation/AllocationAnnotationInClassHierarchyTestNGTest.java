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
package org.quickperf.testng.jvm.allocation;

import org.quickperf.testng.TestNGTests;
import org.quickperf.testng.TestNGTests.TestNGTestsResult;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationAnnotationInClassHierarchyTestNGTest {

    @Test public void
    concrete_class_inheriting_allocation_annotation_should_measure_allocation() {

        // GIVEN
        Class<?> testClass = TestNGConcreteClassExtendingAbstract.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining(
                "Expected heap allocation (test method thread) to be less than 439 bytes but is 440 bytes");

    }

}
