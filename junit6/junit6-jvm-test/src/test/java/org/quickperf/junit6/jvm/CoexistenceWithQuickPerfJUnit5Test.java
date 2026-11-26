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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.SqlRecorderRegistry;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// quick-perf-junit5 can end up on the class path of a JUnit 6 test, during a migration for example. If both extensions
// recorded a test, their SQL recorders would displace each other: quick-perf-junit6 fails a test registered with both,
// before any test method is executed and before quick-perf-junit5 starts a JVM. The check fires in beforeAll, beforeEach,
// beforeTestExecution or before a dynamic test, depending on the order of registration.
class CoexistenceWithQuickPerfJUnit5Test {

    private static final String BOTH_EXTENSIONS_REGISTERED =
            "The QuickPerf JUnit 5 extension (quick-perf-junit5) and the QuickPerf JUnit 6 extension (quick-perf-junit6)"
          + " are both registered";

    private static final String AUTODETECTION_ENABLED = "junit.jupiter.extensions.autodetection.enabled";

    private static final String AUTODETECTION_EXCLUDE = "junit.jupiter.extensions.autodetection.exclude";

    // Surefire sets a private java.io.tmpdir for this module: quick-perf-junit5 creates a working folder there for a
    // test needing a specific JVM
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

    // Counts the executions of the test methods of each fixture class
    static class CountingExecutions {

        private static final Map<Class<?>, AtomicInteger> TEST_METHOD_EXECUTIONS = new ConcurrentHashMap<>();

        void countTheTestMethodExecution() {
            executionsOf(getClass()).incrementAndGet();
        }

        static int testMethodExecutionsOf(Class<?> testClass) {
            return executionsOf(testClass).get();
        }

        private static AtomicInteger executionsOf(Class<?> testClass) {
            return TEST_METHOD_EXECUTIONS.computeIfAbsent(testClass, key -> new AtomicInteger());
        }

    }

    static class NotAnnotated extends CountingExecutions {

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

        @DisplaySql
        @Test
        void another_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_the_tests_with_both_extensions_auto_detected() {

        // GIVEN
        Map<String, String> configurationParameters = Collections.singletonMap(AUTODETECTION_ENABLED, "true");

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(NotAnnotated.class, configurationParameters).run();

        // THEN: in beforeAll or beforeEach, depending on the order of the class path
        assertThat(result.getNumberOfFailures()).isPositive();
        assertThatNothingWasExecutedOrLeft(NotAnnotated.class, result);

    }

    static class OtherNotAnnotated extends CountingExecutions {

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

        @DisplaySql
        @Test
        void another_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_execute_the_tests_with_quick_perf_junit5_excluded_from_the_autodetection() {

        // GIVEN
        Map<String, String> configurationParameters = new HashMap<>();
        configurationParameters.put(AUTODETECTION_ENABLED, "true");
        configurationParameters.put(AUTODETECTION_EXCLUDE, "org.quickperf.junit5.*");

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(OtherNotAnnotated.class, configurationParameters).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(2);
        assertThat(CountingExecutions.testMethodExecutionsOf(OtherNotAnnotated.class)).isEqualTo(2);
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class)).isNull();

    }

    @QuickPerfTest
    static class JUnit6QuickPerfTest extends CountingExecutions {

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    // JUnit registers the auto-detected extensions first
    @Test
    void should_fail_the_class_with_quick_perf_junit5_auto_detected() {

        // GIVEN
        Map<String, String> configurationParameters = new HashMap<>();
        configurationParameters.put(AUTODETECTION_ENABLED, "true");
        configurationParameters.put(AUTODETECTION_EXCLUDE, "org.quickperf.junit6.*");

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit6QuickPerfTest.class, configurationParameters).run();

        // THEN: in beforeAll, before any test
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getNumberOfStartedTests()).isZero();
        assertThatNothingWasExecutedOrLeft(JUnit6QuickPerfTest.class, result);

    }

    @org.quickperf.junit5.QuickPerfTest
    @QuickPerfTest
    static class JUnit5ThenJUnit6QuickPerfTest extends CountingExecutions {

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

        @DisplaySql
        @Test
        void another_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_the_class_with_quick_perf_junit5_registered_first() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit5ThenJUnit6QuickPerfTest.class).run();

        // THEN: in beforeAll, before any test
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getNumberOfStartedTests()).isZero();
        assertThatNothingWasExecutedOrLeft(JUnit5ThenJUnit6QuickPerfTest.class, result);

    }

    @QuickPerfTest
    @org.quickperf.junit5.QuickPerfTest
    static class JUnit6ThenJUnit5QuickPerfTest extends CountingExecutions {

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

        @DisplaySql
        @Test
        void another_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_each_test_with_quick_perf_junit6_registered_first() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit6ThenJUnit5QuickPerfTest.class).run();

        // THEN: in beforeEach, before quick-perf-junit5 builds its context
        assertThat(result.getNumberOfFailures()).isEqualTo(2);
        assertThatNothingWasExecutedOrLeft(JUnit6ThenJUnit5QuickPerfTest.class, result);

    }

    @org.quickperf.junit5.QuickPerfTest
    static class JUnit5OnTheClassAndJUnit6OnTheMethod extends CountingExecutions {

        @ExtendWith(org.quickperf.junit6.QuickPerfTestExtension.class)
        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_quick_perf_junit6_registered_on_its_method() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit5OnTheClassAndJUnit6OnTheMethod.class).run();

        // THEN: in beforeEach, after quick-perf-junit5 built its context, which it releases in afterEach
        assertThat(result.getNumberOfFailures()).isOne();
        assertThatNothingWasExecutedOrLeft(JUnit5OnTheClassAndJUnit6OnTheMethod.class, result);

    }

    @QuickPerfTest
    static class JUnit6OnTheClassAndJUnit5OnTheMethod extends CountingExecutions {

        @ExtendWith(org.quickperf.junit5.QuickPerfTestExtension.class)
        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_quick_perf_junit5_registered_on_its_method() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit6OnTheClassAndJUnit5OnTheMethod.class).run();

        // THEN: in beforeTestExecution, before any interceptor
        assertThat(result.getNumberOfFailures()).isOne();
        assertThatNothingWasExecutedOrLeft(JUnit6OnTheClassAndJUnit5OnTheMethod.class, result);

    }

    static class SwallowingEveryException implements TestExecutionExceptionHandler {

        @Override
        public void handleTestExecutionException(ExtensionContext context, Throwable throwable) {
        }

    }

    @QuickPerfTest
    @ExtendWith(SwallowingEveryException.class)
    static class JUnit5OnTheMethodWithAHandlerSwallowingEveryException extends CountingExecutions {

        @ExtendWith(org.quickperf.junit5.QuickPerfTestExtension.class)
        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    // The exception is thrown by a beforeTestExecution callback: JUnit does not pass it to the exception handlers
    @Test
    void should_fail_a_test_registered_with_both_extensions_whatever_the_exception_handlers() {

        // WHEN
        JUnit5TestsResult result =
                JUnit5Tests.createInstance(JUnit5OnTheMethodWithAHandlerSwallowingEveryException.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThatNothingWasExecutedOrLeft(JUnit5OnTheMethodWithAHandlerSwallowingEveryException.class, result);

    }

    @QuickPerfTest
    static class JUnit5InAStaticField extends CountingExecutions {

        @RegisterExtension
        static org.quickperf.junit5.QuickPerfTestExtension junit5Extension =
                new org.quickperf.junit5.QuickPerfTestExtension();

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_quick_perf_junit5_in_a_static_field() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit5InAStaticField.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThatNothingWasExecutedOrLeft(JUnit5InAStaticField.class, result);

    }

    // No beforeAll callback for an extension of a non-static field
    static class JUnit6InANonStaticField extends CountingExecutions {

        @RegisterExtension
        org.quickperf.junit6.QuickPerfTestExtension junit6Extension = new org.quickperf.junit6.QuickPerfTestExtension();

        @DisplaySql
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    @Test
    void should_fail_a_test_with_quick_perf_junit6_in_a_non_static_field_and_quick_perf_junit5_auto_detected() {

        // GIVEN
        Map<String, String> configurationParameters = new HashMap<>();
        configurationParameters.put(AUTODETECTION_ENABLED, "true");
        configurationParameters.put(AUTODETECTION_EXCLUDE, "org.quickperf.junit6.*");

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit6InANonStaticField.class, configurationParameters).run();

        // THEN: in beforeEach, after quick-perf-junit5 built its context, which it releases in afterEach
        assertThat(result.getNumberOfFailures()).isOne();
        assertThatNothingWasExecutedOrLeft(JUnit6InANonStaticField.class, result);

    }

    @org.quickperf.junit5.QuickPerfTest
    static class JUnit5ClassWithAJUnit6NestedClass {

        @Nested
        @QuickPerfTest
        class JUnit6NestedClass {

            @DisplaySql
            @Test
            void a_test() {
                CountingExecutions.executionsOf(JUnit5ClassWithAJUnit6NestedClass.class).incrementAndGet();
            }

        }

    }

    @Test
    void should_fail_a_quick_perf_junit6_nested_class_in_a_quick_perf_junit5_class() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(JUnit5ClassWithAJUnit6NestedClass.class).run();

        // THEN: in the beforeAll callback of the nested class
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getNumberOfStartedTests()).isZero();
        assertThatNothingWasExecutedOrLeft(JUnit5ClassWithAJUnit6NestedClass.class, result);

    }

    // quick-perf-junit5 would execute this test in a specific JVM
    @QuickPerfTest
    @org.quickperf.junit5.QuickPerfTest
    static class TestNeedingASpecificJvm extends CountingExecutions {

        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() {
            countTheTestMethodExecution();
            throw JvmExecutionReport.reportExecutionOf("a_test");
        }

    }

    @Test
    void should_fail_a_test_registered_with_both_extensions_before_quick_perf_junit5_starts_a_jvm() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestNeedingASpecificJvm.class).run();

        // THEN: in beforeEach, before quick-perf-junit5 builds a context, and before the annotation check
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getJvmExecutionReports()).as("tests executed in any JVM").isEmpty();
        assertThat(result.getErrorReport()).doesNotContain("@HeapSize needs QuickPerf to start a dedicated JVM");
        assertThat(quickPerfWorkingFolders()).as("working folders created by quick-perf-junit5")
                                             .isSubsetOf(WORKING_FOLDERS_BEFORE_THE_TESTS);
        assertThatNothingWasExecutedOrLeft(TestNeedingASpecificJvm.class, result);

    }

    // quick-perf-junit5 registered first: quick-perf-junit6 would fail the class in beforeAll
    @DisableQuickPerf
    @org.quickperf.junit5.QuickPerfTest
    @QuickPerfTest
    static class QuickPerfDisabledOnTheClass extends CountingExecutions {

        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    // quick-perf-junit6 registered first: it would fail the test in beforeEach
    @QuickPerfTest
    @org.quickperf.junit5.QuickPerfTest
    static class QuickPerfDisabledOnTheTestMethod extends CountingExecutions {

        @DisableQuickPerf
        @Test
        void a_test() {
            countTheTestMethodExecution();
        }

    }

    // With QuickPerf disabled, neither extension records SQL: their recordings cannot mix
    @Test
    void should_execute_a_test_registered_with_both_extensions_if_quickperf_is_disabled() {

        // WHEN
        JUnit5TestsResult classResult = JUnit5Tests.createInstance(QuickPerfDisabledOnTheClass.class).run();
        JUnit5TestsResult methodResult = JUnit5Tests.createInstance(QuickPerfDisabledOnTheTestMethod.class).run();

        // THEN
        assertThat(classResult.getNumberOfFailures()).as(classResult.getErrorReport()).isZero();
        assertThat(methodResult.getNumberOfFailures()).as(methodResult.getErrorReport()).isZero();
        assertThat(CountingExecutions.testMethodExecutionsOf(QuickPerfDisabledOnTheClass.class)).isOne();
        assertThat(CountingExecutions.testMethodExecutionsOf(QuickPerfDisabledOnTheTestMethod.class)).isOne();

    }

    // quick-perf-junit5, registered on the test factory method only, marks the extension context in beforeEach, after
    // quick-perf-junit6 checked it: quick-perf-junit6, registered first, fails each dynamic test before it is executed
    @QuickPerfTest
    static class TestFactoryWithJUnit5OnItsMethod extends CountingExecutions {

        @ExtendWith(org.quickperf.junit5.QuickPerfTestExtension.class)
        @DisplaySql
        @TestFactory
        List<DynamicTest> a_test_factory() {
            List<DynamicTest> dynamicTests = new ArrayList<>();
            dynamicTests.add(DynamicTest.dynamicTest("a dynamic test", this::countTheTestMethodExecution));
            dynamicTests.add(DynamicTest.dynamicTest("another dynamic test", this::countTheTestMethodExecution));
            return dynamicTests;
        }

    }

    @Test
    void should_fail_each_dynamic_test_of_a_test_factory_registered_with_both_extensions() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestFactoryWithJUnit5OnItsMethod.class).run();

        // THEN: before each dynamic test
        assertThat(result.getNumberOfFailures()).isEqualTo(2);
        assertThatNothingWasExecutedOrLeft(TestFactoryWithJUnit5OnItsMethod.class, result);

    }

    private static void assertThatNothingWasExecutedOrLeft(Class<?> testClass, JUnit5TestsResult result) {
        assertThat(result.getErrorReport()).contains("ExtensionConfigurationException")
                                           .contains(BOTH_EXTENSIONS_REGISTERED)
                                           .contains("junit.jupiter.extensions.autodetection.exclude=org.quickperf.junit5.*");
        assertThat(CountingExecutions.testMethodExecutionsOf(testClass)).as("executions of the test methods").isZero();
        // On this thread, as the callbacks
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread of the test")
                .isNull();
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
