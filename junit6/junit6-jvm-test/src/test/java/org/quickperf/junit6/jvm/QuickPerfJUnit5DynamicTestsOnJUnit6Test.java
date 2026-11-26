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
package org.quickperf.junit6.jvm;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

// quick-perf-junit5 on JUnit 6, where JUnit calls only the 3-argument interceptDynamicTest method: JUnit 6 removed the
// 2-argument one. Executed by its own Surefire execution, with its own java.io.tmpdir: quick-perf-junit5 leaves there
// the working folder of each test executed in a dedicated JVM, and the other tests of this module check that there is
// none in theirs.
class QuickPerfJUnit5DynamicTestsOnJUnit6Test {

    static {
        try {
            // quick-perf-junit5 creates the working folders there
            Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @QuickPerfTest
    static class DynamicTestLastingLongerThanExpected {

        @ExpectMaxExecutionTime(milliSeconds = 10)
        @TestFactory
        DynamicTest a_test_factory() {
            return dynamicTest("a dynamic test lasting 200 ms", () -> Thread.sleep(200));
        }

    }

    @Test
    void should_check_the_performance_of_a_dynamic_test() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(DynamicTestLastingLongerThanExpected.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as("failures.%s", result.getErrorReport()).isOne();
        assertThat(result.getErrorReport()).contains("Execution time of the test method expected to be less than");

    }

    @QuickPerfTest
    static class DynamicTestsNeedingADedicatedJvm {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        List<DynamicTest> dynamic_tests() {
            return List.of(dynamicTest("apple", () -> { throw reportExecutionOf("apple"); })
                         , dynamicTest("banana", () -> { throw reportExecutionOf("banana"); }));
        }

    }

    @Test
    void should_execute_each_dynamic_test_in_its_own_dedicated_jvm() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(DynamicTestsNeedingADedicatedJvm.class).run();

        // THEN
        assertThat(result.getNumberOfFailures())
                .as("Each dynamic test should fail with the report of its execution.%s", result.getErrorReport())
                .isEqualTo(2);
        List<JvmExecutionReport> jvmExecutionReports = result.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest").containsExactlyInAnyOrder("apple", "banana");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

}
