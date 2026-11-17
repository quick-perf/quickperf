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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * The JUnit unique id of a test executed through a <code>@Suite</code> starts with segments of the suite engine. The
 * specific JVM executes the test with the Jupiter engine only, without the <code>@BeforeSuite</code> and
 * <code>@AfterSuite</code> methods, which were already executed in the test JVM. Its messages give this unique id,
 * without the segments of the suite engine.
 * <p>
 * The test methods of {@link TestsInASpecificJvm} fail on purpose with a {@link JvmExecutionReport}, to tell where they
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

    @QuickPerfTest
    static class TestFactoryFailingInTheSpecificJvms {

        private final List<String> unreleasedResources = new ArrayList<>();

        // The test JVM does not execute the dynamic tests: this check only fails in the specific JVM of the dynamic test
        // not releasing its resource
        @AfterEach
        public void after_each() {
            assertThat(unreleasedResources).as("Resources not released by the dynamic tests").isEmpty();
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        public List<DynamicTest> dynamic_tests() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(dynamicTest("dynamic test not releasing its resource", () -> unreleasedResources.add("resource")));
            if (!SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
                dynamicTests.add(dynamicTest("dynamic test missing in the specific JVM", () -> { }));
            }
            return dynamicTests;
        }

    }

    @Suite
    @SelectClasses(TestFactoryFailingInTheSpecificJvms.class)
    static class SuiteOfATestFactoryFailingInTheSpecificJvms {
    }

    @Test public void
    should_give_the_unique_id_executed_by_the_specific_jvm_in_the_messages_about_a_dynamic_test_of_a_suite() {

        // GIVEN
        Class<?> suiteClass = SuiteOfATestFactoryFailingInTheSpecificJvms.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(suiteClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each dynamic test should fail in its JVM.%s", errorReport)
                .isEqualTo(2);

        String testFactoryUniqueId = "[engine:junit-jupiter]/[class:" + TestFactoryFailingInTheSpecificJvms.class.getName() + "]"
                                   + "/[test-factory:dynamic_tests()]";
        assertThat(errorReport).contains("JUnit reported a failure of dynamic_tests() after the dynamic test in the specific JVM"
                                       + " where QuickPerf executes " + testFactoryUniqueId + "/[dynamic-test:#1]")
                               .contains("Resources not released by the dynamic tests")
                               .contains("JUnit started 0 tests instead of 1 in the specific JVM where QuickPerf executes "
                                       + testFactoryUniqueId + "/[dynamic-test:#2]")
                               .doesNotContain("[engine:junit-platform-suite]");

    }

    @QuickPerfTest
    static class ParameterizedTestInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        public void parameterized_test(int value, TestInfo testInfo) {
            throw reportExecutionOf(testInfo.getDisplayName());
        }

    }

    @Suite
    @SelectClasses(ParameterizedTestInASpecificJvm.class)
    @ConfigurationParameter(key = "junit.jupiter.params.displayname.default", value = "invocation {index} with {arguments}")
    static class SuiteConfiguringTheDisplayNames {
    }

    @Test public void
    should_execute_each_invocation_of_a_suite_with_the_display_names_configured_by_the_suite() {

        // GIVEN
        Class<?> suiteClass = SuiteConfiguringTheDisplayNames.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(suiteClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(errorReport)
                                       .extracting("executedTest")
                                       .containsExactlyInAnyOrder("invocation 1 with 1", "invocation 2 with 2");
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

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
