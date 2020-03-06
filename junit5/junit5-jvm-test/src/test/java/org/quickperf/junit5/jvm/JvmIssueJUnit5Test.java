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

package org.quickperf.junit5.jvm;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class JvmIssueJUnit5Test {

    @QuickPerfTest
    public static class TestClass {

        @HeapSize(value = 2, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_with_oom() {
            ArrayList<Object> data = new ArrayList<>(1000);
        }

    }

    @Test public void
    should_report_jvm_issue() {

        // GIVEN
        Class<TestClass> testClass = TestClass.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains(
                "Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread \"main\"");

    }


}
