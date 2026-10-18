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
package org.quickperf.junit5.suite;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * The JUnit unique id of a test executed through a <code>@Suite</code> starts with segments of the suite engine. The
 * specific JVM executes the test with the Jupiter engine only, without the <code>@BeforeSuite</code> and
 * <code>@AfterSuite</code> methods, which were already executed in the test JVM.
 * <p>
 * The test methods of the nested test classes fail on purpose with a {@link JvmExecutionReport}, to tell where they
 * were executed.
 */
public class SuiteInNewJvmJUnit5Test {

    private static final AtomicInteger BEFORE_SUITE_EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

    @QuickPerfTest
    static class TestsInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(TestInfo testInfo) {
            throw reportExecutionOf("@BeforeSuite executions: " + BEFORE_SUITE_EXECUTIONS_IN_THIS_JVM.get());
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @RepeatedTest(2)
        public void repeated_test(RepetitionInfo repetitionInfo) {
            throw reportExecutionOf("@BeforeSuite executions: " + BEFORE_SUITE_EXECUTIONS_IN_THIS_JVM.get());
        }

    }

    @Suite
    @SelectClasses(TestsInASpecificJvm.class)
    static class SuiteOfTestsInASpecificJvm {

        @BeforeSuite
        public static void before_suite() {
            BEFORE_SUITE_EXECUTIONS_IN_THIS_JVM.incrementAndGet();
        }

    }

    @Suite
    @SelectClasses(SuiteOfTestsInASpecificJvm.class)
    static class SuiteOfSuite {
    }

    @Test public void
    should_execute_each_test_of_a_suite_in_its_own_jvm_without_the_before_suite_methods() {

        // GIVEN
        Class<?> suiteClass = SuiteOfTestsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(suiteClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThatEachTestWasExecutedInItsOwnNewJvmWithoutTheBeforeSuiteMethods(jUnit5TestsResult);

    }

    @Test public void
    should_execute_each_test_of_a_nested_suite_in_its_own_jvm_without_the_before_suite_methods() {

        // GIVEN
        Class<?> suiteClass = SuiteOfSuite.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(suiteClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThatEachTestWasExecutedInItsOwnNewJvmWithoutTheBeforeSuiteMethods(jUnit5TestsResult);

    }

    private static void assertThatEachTestWasExecutedInItsOwnNewJvmWithoutTheBeforeSuiteMethods(JUnit5TestsResult jUnit5TestsResult) {
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each test should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactly("@BeforeSuite executions: 0"
                                                      , "@BeforeSuite executions: 0"
                                                      , "@BeforeSuite executions: 0");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
    }

}
