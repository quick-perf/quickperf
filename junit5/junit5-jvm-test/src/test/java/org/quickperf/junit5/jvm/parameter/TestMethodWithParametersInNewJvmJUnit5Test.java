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
package org.quickperf.junit5.jvm.parameter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasTheTestExecutedInItsNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * QuickPerf executes a test method having an annotation that needs a specific JVM (<code>@HeapSize</code>,
 * <code>@ExpectMaxHeapAllocation</code>...) in a new JVM. There, JUnit and its extensions resolve the parameters of the
 * test method (<code>TestInfo</code>, <code>@TempDir</code>, a Spring bean, a Mockito mock...).
 * <p>
 * Some test methods of the nested test classes fail on purpose with a {@link JvmExecutionReport}, to tell where they
 * were executed. Others fail if their JVM executes them more than once. QuickPerf fails a test that JUnit does not
 * execute in the specific JVM.
 */
public class TestMethodWithParametersInNewJvmJUnit5Test {

    @QuickPerfTest
    static class TestMethodHavingATestInfoParameterInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(TestInfo testInfo) {
            throw reportExecutionOf(testInfo.getDisplayName());
        }

    }

    @Test public void
    should_execute_a_test_method_having_a_test_info_parameter_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodHavingATestInfoParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(jUnit5TestsResult.getErrorReport())
                                       .extracting("executedTest")
                                       .containsExactly("test_method(TestInfo)");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class TestMethodHavingATestReporterParameterInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(TestReporter testReporter) {
            testReporter.publishEntry("key", "value");
            throw reportExecutionOf("entry published");
        }

    }

    @Test public void
    should_execute_a_test_method_having_a_test_reporter_parameter_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodHavingATestReporterParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(jUnit5TestsResult.getErrorReport())
                                       .extracting("executedTest")
                                       .containsExactly("entry published");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class TestMethodHavingATempDirParameterInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(@TempDir Path temporaryDirectory) throws IOException {
            Path file = temporaryDirectory.resolve("file.txt");
            Files.write(file, Collections.singletonList("content"), StandardCharsets.UTF_8);
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            throw reportExecutionOf("read from the temporary directory: " + lines);
        }

    }

    @Test public void
    should_execute_a_test_method_having_a_temp_dir_parameter_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodHavingATempDirParameterInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(jUnit5TestsResult.getErrorReport())
                                       .extracting("executedTest")
                                       .containsExactly("read from the temporary directory: [content]");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);

    }

    // As the extensions of Spring or Mockito, which resolve beans or mocks
    static class StringParameterResolver implements ParameterResolver {

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == String.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return "resolved by an extension";
        }

    }

    @QuickPerfTest
    @ExtendWith(StringParameterResolver.class)
    static class TestMethodHavingAParameterResolvedByAnExtensionInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(String parameter) {
            throw reportExecutionOf("parameter " + parameter);
        }

    }

    @Test public void
    should_execute_a_test_method_having_a_parameter_resolved_by_an_extension_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodHavingAParameterResolvedByAnExtensionInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(jUnit5TestsResult.getErrorReport())
                                       .extracting("executedTest")
                                       .containsExactly("parameter resolved by an extension");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class TestMethodsHavingParametersInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_having_several_parameters(TestInfo testInfo
                                                        , TestReporter testReporter
                                                        , @TempDir Path temporaryDirectory) {
            throw reportExecutionOf(testInfo.getDisplayName());
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_having_a_parameter(TestInfo testInfo) {
            throw reportExecutionOf(testInfo.getDisplayName());
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_without_parameter() {
            throw reportExecutionOf("test_method_without_parameter()");
        }

    }

    @Test public void
    should_execute_each_test_method_of_a_test_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodsHavingParametersInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each test method should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("test_method_having_several_parameters(TestInfo, TestReporter, Path)"
                                                                , "test_method_having_a_parameter(TestInfo)"
                                                                , "test_method_without_parameter()");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestWasTheTestExecutedInItsNewJvm(jvmExecutionReports);

    }

    @QuickPerfTest
    static class TestMethodsHavingParametersAndFailingIfExecutedAgainInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_having_a_test_info_parameter(TestInfo testInfo) {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne();
            assertThat(testInfo.getDisplayName()).isEqualTo("test_method_having_a_test_info_parameter(TestInfo)");
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_having_a_temp_dir_parameter(@TempDir Path temporaryDirectory) throws IOException {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne();
            Path file = temporaryDirectory.resolve("file.txt");
            Files.write(file, Collections.singletonList("content"), StandardCharsets.UTF_8);
            assertThat(Files.readAllLines(file, StandardCharsets.UTF_8)).containsExactly("content");
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_having_several_parameters(TestInfo testInfo
                                                        , TestReporter testReporter
                                                        , @TempDir Path temporaryDirectory) {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM").isOne();
            assertThat(temporaryDirectory).isDirectory();
        }

    }

    @Test public void
    should_execute_each_test_method_having_parameters_once_in_its_jvm() {

        // GIVEN
        Class<?> testClass = TestMethodsHavingParametersAndFailingIfExecutedAgainInTheSameJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(3);

    }

    @QuickPerfTest
    static class TestMethodHavingATestInfoParameterAndAllocatingThreeMegaBytes {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method(TestInfo testInfo) {
            byte[] allocatedBytes = new byte[3 * 1024 * 1024];
        }

    }

    @Test public void
    should_measure_the_heap_allocation_of_a_test_method_having_a_parameter() {

        // GIVEN
        Class<?> testClass = TestMethodHavingATestInfoParameterAndAllocatingThreeMegaBytes.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isOne();

        assertThat(errorReport).contains("Expected heap allocation (test method thread) to be less than 1.00 Mega bytes but is 3.");

    }

}
