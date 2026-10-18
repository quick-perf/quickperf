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
package org.quickperf.junit5.jvm.testtemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * QuickPerf executes a test having an annotation that needs a specific JVM (<code>@HeapSize</code>,
 * <code>@ExpectMaxHeapAllocation</code>...) in a specific JVM, which has to execute this test once. These tests check
 * the result of tests that the specific JVM does not execute as the test JVM would.
 */
public class TestExecutionChecksInNewJvmJUnit5Test {

    private static boolean executingInTheSpecificJvm() {
        return SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
    }

    @QuickPerfTest
    static class TestDisabledInTheSpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @DisabledIfSystemProperty(named = "quickPerfToExecInASpecificJvm", matches = "true")
        @Test
        public void test_disabled_in_the_specific_jvm() {
        }

    }

    @Test public void
    should_fail_if_a_condition_disables_the_test_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = TestDisabledInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test should fail because the specific JVM did not execute it.%s", errorReport)
                .isOne();

        assertThat(errorReport).contains("JUnit started 0 tests instead of 1 in the specific JVM")
                               .contains("[method:test_disabled_in_the_specific_jvm()]")
                               .contains("test_disabled_in_the_specific_jvm() skipped: ")
                               .contains("quickPerfToExecInASpecificJvm")
                               .contains("@JvmOptions(\"-Dname=value\")");

    }

    @QuickPerfTest
    static class ParameterizedTestWithFewerArgumentsInTheSpecificJvm {

        static IntStream values() {
            return executingInTheSpecificJvm() ? IntStream.of(1, 2) : IntStream.of(1, 2, 3);
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest
        @MethodSource("values")
        public void parameterized_test(int value) {
        }

    }

    @Test public void
    should_fail_the_test_template_invocation_that_does_not_exist_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedTestWithFewerArgumentsInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the third invocation should fail, because it does not exist in the specific JVM.%s"
                  , errorReport)
                .isOne();

        assertThat(errorReport).contains("JUnit started 0 tests instead of 1 in the specific JVM")
                               .contains("[test-template-invocation:#3]")
                               .contains("provides other arguments in the specific JVM")
                               .doesNotContain("UnexpectedQuickPerfException");

    }

    @QuickPerfTest
    static class BeforeAllMethodFailingInTheSpecificJvm {

        @BeforeAll
        public static void before_all() {
            if (executingInTheSpecificJvm()) {
                throw new IllegalStateException("@BeforeAll failure in the specific JVM");
            }
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
        }

    }

    @Test public void
    should_report_the_failure_of_a_before_all_method_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = BeforeAllMethodFailingInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isOne();

        assertThat(errorReport).contains("JUnit started 0 tests instead of 1 in the specific JVM")
                               .contains("java.lang.IllegalStateException: @BeforeAll failure in the specific JVM")
                               .doesNotContain("UnexpectedQuickPerfException");

    }

    @QuickPerfTest
    static class AssumptionFailingInABeforeEachMethodInTheSpecificJvm {

        @BeforeEach
        public void before_each() {
            assumeFalse(executingInTheSpecificJvm(), "Assumption failing in the specific JVM");
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
        }

    }

    @Test public void
    should_report_a_test_aborted_in_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeEachMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should be aborted, as in the specific JVM.")
                .isOne();

    }

    // As an AssertJ assumption, which throws a TestAbortedException having an AssertionFailedError cause
    private static void assumeNotExecutingInTheSpecificJvm(String description) {
        if (executingInTheSpecificJvm()) {
            String assertionMessage = "[" + description + "] Expecting value to be false but was true";
            AssertionFailedError assertionFailedError = new AssertionFailedError(assertionMessage, false, true);
            throw new TestAbortedException("assumption was not met due to: " + assertionMessage, assertionFailedError);
        }
    }

    @QuickPerfTest
    static class AssumptionWithACauseFailingInABeforeEachMethodInTheSpecificJvm {

        @BeforeEach
        public void before_each() {
            assumeNotExecutingInTheSpecificJvm("Database available");
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
        }

    }

    @Test public void
    should_report_a_test_aborted_with_a_cause_in_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionWithACauseFailingInABeforeEachMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should be aborted, as in the specific JVM.")
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("TestAbortedException: assumption was not met due to: [Database available]")
                .contains("Caused by: org.opentest4j.AssertionFailedError: [Database available]");

    }

    @QuickPerfTest
    static class AssumptionFailingInABeforeAllMethodInTheSpecificJvm {

        @BeforeAll
        public static void before_all() {
            assumeFalse(executingInTheSpecificJvm(), "Assumption failing in a @BeforeAll method of the specific JVM");
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
        }

    }

    @Test public void
    should_report_a_test_aborted_by_a_before_all_method_of_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeAllMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should be aborted, as its class in the specific JVM.")
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing in a @BeforeAll method of the specific JVM");

    }

    @QuickPerfTest
    static class AssumptionFailingInAnArgumentsSourceInTheSpecificJvm {

        static Stream<Integer> values() {
            assumeFalse(executingInTheSpecificJvm(), "Assumption failing in an arguments source of the specific JVM");
            return Stream.of(1, 2);
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest
        @MethodSource("values")
        public void parameterized_test(int value) {
        }

    }

    @Test public void
    should_report_the_invocations_aborted_by_an_arguments_source_of_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnArgumentsSourceInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("Each invocation should be aborted, as its parameterized test in the specific JVM.")
                .isEqualTo(2);

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("Assumption failing in an arguments source of the specific JVM");

    }

    @QuickPerfTest
    static class AssumptionFailingInAnAfterEachMethodInTheSpecificJvm {

        @AfterEach
        public void after_each() {
            assumeFalse(executingInTheSpecificJvm(), "Assumption failing in an @AfterEach method of the specific JVM");
        }

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_allocating_3_mega_bytes() {
            byte[] allocatedBytes = new byte[3 * 1024 * 1024];
        }

    }

    @Test public void
    should_report_the_performance_issue_of_a_test_aborted_after_its_test_method_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterEachMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The performance issue should fail the test, as when the test does not need a specific JVM.%s"
                  , errorReport)
                .isOne();

        assertThat(errorReport).contains("Expected heap allocation (test method thread) to be less than 1.00 Mega bytes")
                               .contains("Assumption failing in an @AfterEach method of the specific JVM");

    }

    @QuickPerfTest
    static class AssumptionWithACauseFailingInTheTestMethodInTheSpecificJvm {

        @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            assumeNotExecutingInTheSpecificJvm("Server available");
        }

    }

    @Test public void
    should_report_a_test_aborted_by_its_test_method_in_the_specific_jvm_as_aborted() {

        // GIVEN
        Class<?> testClass = AssumptionWithACauseFailingInTheTestMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The test should be aborted, as in the specific JVM.")
                .isOne();

        assertThat(jUnit5TestsResult.getAbortReport())
                .contains("TestAbortedException: assumption was not met due to: [Server available]");

    }

    // Like junit-pioneer @RetryingTest(3): provides the attempt n + 1 only if the attempt n failed, and aborts a failed
    // attempt if another attempt remains
    static class RetryingTestTemplate implements TestTemplateInvocationContextProvider {

        private static final int MAX_ATTEMPTS = 3;

        @Override
        public boolean supportsTestTemplate(ExtensionContext extensionContext) {
            return true;
        }

        @Override
        public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
            Iterator<TestTemplateInvocationContext> attempts = new Attempts();
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(attempts, Spliterator.ORDERED), false);
        }

        private static class Attempts implements Iterator<TestTemplateInvocationContext> {

            private int attemptCount;

            private int failedAttemptCount;

            @Override
            public boolean hasNext() {
                return attemptCount == failedAttemptCount && attemptCount < MAX_ATTEMPTS;
            }

            @Override
            public TestTemplateInvocationContext next() {
                int attempt = ++attemptCount;
                return new TestTemplateInvocationContext() {
                    @Override
                    public List<Extension> getAdditionalExtensions() {
                        TestExecutionExceptionHandler retryIfAnAttemptRemains = (extensionContext, throwable) -> {
                            failedAttemptCount++;
                            if (attempt < MAX_ATTEMPTS) {
                                throw new TestAbortedException("Attempt " + attempt + " failed, retrying", throwable);
                            }
                            throw throwable;
                        };
                        return Collections.singletonList(retryIfAnAttemptRemains);
                    }
                };
            }

        }

    }

    @QuickPerfTest
    static class AlwaysFailingRetryingTestInASpecificJvm {

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @TestTemplate
        @ExtendWith(RetryingTestTemplate.class)
        public void always_failing_test() {
            throw new AssertionError("Failure of the test method");
        }

    }

    @Test public void
    should_fail_an_always_failing_test_template_retrying_its_failed_invocations() {

        // GIVEN
        Class<?> testClass = AlwaysFailingRetryingTestInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The last attempt should fail, and no attempt should pass.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfAbortedTests())
                .as("The first two attempts should be aborted to be retried")
                .isEqualTo(2);

        // Only the first attempt exists in a specific JVM, where the previous attempts were not executed
        assertThat(errorReport).contains("JUnit started 0 tests instead of 1 in the specific JVM")
                               .contains("[test-template-invocation:#3]")
                               .contains("retry extensions such as junit-pioneer @RetryingTest");

    }

    @QuickPerfTest
    static class OverloadedTestMethodsInASpecificJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        // The test methods fail to report which method the specific JVM executed

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void overloaded_test_method() {
            throw new AssertionError("Execution " + EXECUTIONS_IN_THIS_JVM.incrementAndGet()
                                   + " in this JVM: overloaded_test_method()");
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        public void overloaded_test_method(int value) {
            throw new AssertionError("Execution " + EXECUTIONS_IN_THIS_JVM.incrementAndGet()
                                   + " in this JVM: overloaded_test_method(" + value + ")");
        }

    }

    @Test public void
    should_execute_the_overloaded_test_method_of_each_test_once_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = OverloadedTestMethodsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isEqualTo(3);

        assertThat(errorReport).contains("Execution 1 in this JVM: overloaded_test_method()")
                               .contains("Execution 1 in this JVM: overloaded_test_method(1)")
                               .contains("Execution 1 in this JVM: overloaded_test_method(2)");

    }

    @QuickPerfTest
    static class OutOfMemoryErrorInTheTestMethodInTheSpecificJvm {

        private static final long TWENTY_SECONDS = 20_000L;

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method_throwing_an_out_of_memory_error_and_leaving_a_non_daemon_thread() {
            // This thread keeps the specific JVM alive if the JVM does not end with System.exit
            Thread nonDaemonThread = new Thread(() -> sleep(TWENTY_SECONDS));
            nonDaemonThread.setDaemon(false);
            nonDaemonThread.start();
            throw new OutOfMemoryError("Out of memory error in the specific JVM");
        }

        private static void sleep(long durationInMs) {
            try {
                Thread.sleep(durationInMs);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
            }
        }

    }

    @Test public void
    should_report_an_out_of_memory_error_of_the_test_method_without_waiting_for_the_threads_of_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = OutOfMemoryErrorInTheTestMethodInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        long start = System.nanoTime();
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();
        long durationInSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start);

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(errorReport)
                .isOne();

        assertThat(errorReport).contains("java.lang.OutOfMemoryError: Out of memory error in the specific JVM");

        assertThat(durationInSeconds)
                .as("The specific JVM should end without waiting for the non-daemon thread of the test")
                .isLessThan(15);

    }

}
