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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.ExpectMaxRSS;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.jvm.jfr.annotation.ProfileJvm;
import org.quickperf.sql.annotation.ExpectSelect;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// quick-perf-junit6 executes the tests in the test JVM: it applies none of the annotations needing a specific JVM. On
// the test method or its class, such an annotation fails the test before anything is executed or built.
class JvmAnnotationsJUnit6Test {

    private static final String EXTENSION_CONFIGURATION_EXCEPTION =
            "org.junit.jupiter.api.extension.ExtensionConfigurationException";

    // Surefire sets a private java.io.tmpdir for this module: QuickPerf could create its working folders there
    private static final Path TEMPORARY_DIRECTORY = createTemporaryDirectory();

    private static Path createTemporaryDirectory() {
        try {
            return Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir")));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // The folders already there: left by a build without clean, or by QuickPerfJUnit5DynamicTestsOnJUnit6Test when
    // -Dtest makes both Surefire executions of this module execute both classes. The tests check that no other appears.
    private static final List<Path> WORKING_FOLDERS_BEFORE_THE_TESTS = quickPerfWorkingFolders();

    // Counts the executions of the @BeforeEach and test methods of each fixture class
    static class CountingExecutions {

        private static final Map<Class<?>, AtomicInteger> BEFORE_EACH_EXECUTIONS = new ConcurrentHashMap<>();

        private static final Map<Class<?>, AtomicInteger> TEST_METHOD_EXECUTIONS = new ConcurrentHashMap<>();

        @BeforeEach
        void count_the_before_each_execution() {
            executionsOf(BEFORE_EACH_EXECUTIONS, getClass()).incrementAndGet();
        }

        void countTheTestMethodExecution() {
            executionsOf(TEST_METHOD_EXECUTIONS, getClass()).incrementAndGet();
        }

        static int beforeEachExecutionsOf(Class<?> testClass) {
            return executionsOf(BEFORE_EACH_EXECUTIONS, testClass).get();
        }

        static int testMethodExecutionsOf(Class<?> testClass) {
            return executionsOf(TEST_METHOD_EXECUTIONS, testClass).get();
        }

        private static AtomicInteger executionsOf(Map<Class<?>, AtomicInteger> executions, Class<?> testClass) {
            return executions.computeIfAbsent(testClass, key -> new AtomicInteger());
        }

    }

    @QuickPerfTest
    static class HeapSizeOnTheTestMethod extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_an_annotation_giving_a_jvm_option() {
        assertThatTheTestFailsBeforeBeingExecuted(HeapSizeOnTheTestMethod.class
                                                , "@HeapSize needs QuickPerf to start a dedicated JVM");
    }

    @QuickPerfTest
    static class ExpectMaxHeapAllocationOnTheTestMethod extends CountingExecutions {

        @ExpectMaxHeapAllocation(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_an_annotation_measuring_the_heap_allocation() {
        assertThatTheTestFailsBeforeBeingExecuted(ExpectMaxHeapAllocationOnTheTestMethod.class
                                                , "@ExpectMaxHeapAllocation needs QuickPerf to start a dedicated JVM");
    }

    @QuickPerfTest
    static class ExpectMaxRssOnTheTestMethod extends CountingExecutions {

        @ExpectMaxRSS(value = 1, unit = AllocationUnit.GIGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_an_annotation_measuring_the_rss() {
        assertThatTheTestFailsBeforeBeingExecuted(ExpectMaxRssOnTheTestMethod.class
                                                , "@ExpectMaxRSS needs QuickPerf to start a dedicated JVM");
    }

    @QuickPerfTest
    static class ProfileJvmOnTheTestMethod extends CountingExecutions {

        @ProfileJvm
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_an_annotation_profiling_the_jvm_with_jfr() {
        assertThatTheTestFailsBeforeBeingExecuted(ProfileJvmOnTheTestMethod.class
                                                , "@ProfileJvm needs QuickPerf to start a dedicated JVM");
    }

    @QuickPerfTest
    @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
    static class HeapSizeOnTheTestClass extends CountingExecutions {

        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

        @Test
        void another_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_each_test_of_a_class_with_an_annotation_needing_a_specific_jvm() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(HeapSizeOnTheTestClass.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isEqualTo(2);
        assertThat(result.getErrorReport()).contains(EXTENSION_CONFIGURATION_EXCEPTION)
                                           .contains("@HeapSize needs QuickPerf to start a dedicated JVM");
        assertThatNothingWasExecutedOrCreated(HeapSizeOnTheTestClass.class);

    }

    @QuickPerfTest
    static class TwoAnnotationsNeedingASpecificJvm extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectMaxHeapAllocation(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_name_every_annotation_needing_a_specific_jvm() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TwoAnnotationsNeedingASpecificJvm.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        // The order of the annotations depends on the reflection
        assertThat(result.getErrorReport()).contains(EXTENSION_CONFIGURATION_EXCEPTION)
                                           .contains("@HeapSize")
                                           .contains("@ExpectMaxHeapAllocation")
                                           .contains("need QuickPerf to start a dedicated JVM")
                                           .contains("it cannot apply them")
                                           .contains("Remove them");
        assertThatNothingWasExecutedOrCreated(TwoAnnotationsNeedingASpecificJvm.class);

    }

    @QuickPerfTest
    static class JvmAnnotationWithASqlAnnotation extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(5)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_only_report_the_annotation_needing_a_specific_jvm_of_a_test_with_a_sql_annotation() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JvmAnnotationWithASqlAnnotation.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains("@HeapSize needs QuickPerf to start a dedicated JVM")
                                           .doesNotContain("select statement");
        assertThatNothingWasExecutedOrCreated(JvmAnnotationWithASqlAnnotation.class);

    }

    @QuickPerfTest
    static class TestFactoryWithAJvmAnnotation extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @TestFactory
        List<DynamicTest> a_test_factory() {
            countTheTestMethodExecution();
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(DynamicTest.dynamicTest("a dynamic test", () -> { }));
            dynamicTests.add(DynamicTest.dynamicTest("another dynamic test", () -> { }));
            return dynamicTests;
        }

    }

    @Test
    void should_fail_a_test_factory_with_an_annotation_needing_a_specific_jvm_once() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestFactoryWithAJvmAnnotation.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as("failures of the test factory").isOne();
        assertThat(result.getNumberOfStartedTests()).as("started dynamic tests").isZero();
        assertThat(result.getErrorReport()).contains("@HeapSize needs QuickPerf to start a dedicated JVM");
        assertThatNothingWasExecutedOrCreated(TestFactoryWithAJvmAnnotation.class);

    }

    @QuickPerfTest
    static class JvmAnnotationWithQuickPerfDisabled extends CountingExecutions {

        @DisableQuickPerf
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test_with_quickperf_disabled() {
            countTheTestMethodExecution();
        }

        @FunctionalIteration
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test_of_a_functional_iteration() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_execute_a_test_with_an_annotation_needing_a_specific_jvm_if_quickperf_is_disabled() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JvmAnnotationWithQuickPerfDisabled.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(2);
        assertThat(CountingExecutions.testMethodExecutionsOf(JvmAnnotationWithQuickPerfDisabled.class)).isEqualTo(2);

    }

    static class SwallowingEveryException implements TestExecutionExceptionHandler {

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) {
        }

    }

    @QuickPerfTest
    @ExtendWith(SwallowingEveryException.class)
    static class JvmAnnotationWithAHandlerSwallowingEveryException extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    // The exception is thrown by a beforeEach callback: JUnit does not pass it to the exception handlers
    @Test
    void should_fail_a_test_with_an_annotation_needing_a_specific_jvm_whatever_the_exception_handlers() {
        assertThatTheTestFailsBeforeBeingExecuted(JvmAnnotationWithAHandlerSwallowingEveryException.class
                                                , "@HeapSize needs QuickPerf to start a dedicated JVM");
    }

    private static void assertThatTheTestFailsBeforeBeingExecuted(Class<?> testClass, String expectedMessage) {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(testClass).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains(EXTENSION_CONFIGURATION_EXCEPTION)
                                           .contains(expectedMessage)
                                           .contains("quick-perf-junit6");
        assertThatNothingWasExecutedOrCreated(testClass);

    }

    private static void assertThatNothingWasExecutedOrCreated(Class<?> testClass) {
        assertThat(CountingExecutions.beforeEachExecutionsOf(testClass)).as("executions of the @BeforeEach method")
                                                                        .isZero();
        assertThat(CountingExecutions.testMethodExecutionsOf(testClass)).as("executions of the test method")
                                                                        .isZero();
        assertThat(quickPerfWorkingFolders()).as("working folders created by QuickPerf")
                                             .isSubsetOf(WORKING_FOLDERS_BEFORE_THE_TESTS);
    }

    private static List<Path> quickPerfWorkingFolders() {
        List<Path> workingFolders = new ArrayList<>();
        try (DirectoryStream<Path> folders = Files.newDirectoryStream(TEMPORARY_DIRECTORY, "QuickPerf-*")) {
            for (Path folder : folders) {
                workingFolders.add(folder);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return workingFolders;
    }

}
