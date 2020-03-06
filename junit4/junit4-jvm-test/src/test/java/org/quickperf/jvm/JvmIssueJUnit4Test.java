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

package org.quickperf.jvm;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class JvmIssueJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
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

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String errorReport = printableResult.toString();

        assertThat(errorReport).contains(
                "Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread \"main\"");

        // We check that the error report does not contain a QuickPerf stack trace
        assertThat(errorReport.replace("org.quickperf.issue.JvmError", "")
                              .replace("org.quickperf.jvm.JvmIssueJUnit4Test", "")
                  )
                .doesNotContain("org.quickperf");

    }

}
