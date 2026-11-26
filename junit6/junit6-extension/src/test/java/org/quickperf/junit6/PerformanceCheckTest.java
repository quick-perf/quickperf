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
package org.quickperf.junit6;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.opentest4j.TestAbortedException;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.issue.PerfIssue;
import org.quickperf.junit6.FakeQuickPerfConfigs.FakeRecorder;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.perfrecording.PerfRecord;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.quickperf.junit6.FakeQuickPerfConfigs.quickPerfConfigsOf;

class PerformanceCheckTest {

    private static final String PERF_ISSUE = "A performance-related property of the test is not respected.";

    @Retention(RetentionPolicy.RUNTIME)
    @interface FailingToStart { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface WithAPerfIssue { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface CountingItsCleanings { }

    // The recorders are public: TestExecutionContext instantiates them from another package. The tests can be executed
    // concurrently: each recorder with counters is used by one test only

    public static class RecorderFailingToStart extends FakeRecorder {

        static final AtomicReference<RuntimeException> START_FAILURE = new AtomicReference<>();

        static final AtomicInteger STOPS = new AtomicInteger();

        @Override
        public void startRecording(TestExecutionContext testExecutionContext) {
            RuntimeException startFailure = new IllegalStateException("The recording cannot start");
            START_FAILURE.set(startFailure);
            throw startFailure;
        }

        @Override
        public void stopRecording(TestExecutionContext testExecutionContext) {
            STOPS.incrementAndGet();
        }

    }

    public static class RecorderOfAnnotationWithAPerfIssue extends FakeRecorder { }

    public static class RecorderCountingItsCleanings extends FakeRecorder {

        static final AtomicInteger CLEANINGS = new AtomicInteger();

        @Override
        public void cleanResources() {
            CLEANINGS.incrementAndGet();
        }

    }

    private static final QuickPerfConfigs QUICK_PERF_CONFIGS = quickPerfConfigsOf(
              new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderFailingToStart.class)
                    .build(FailingToStart.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationWithAPerfIssue.class)
                    .perfMeasureExtractor((PerfRecord perfRecord) -> BooleanMeasure.TRUE)
                    .perfIssueVerifier((annotation, measure) -> new PerfIssue(PERF_ISSUE))
                    .build(WithAPerfIssue.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderCountingItsCleanings.class)
                    .build(CountingItsCleanings.class));

    private static class TestClass {

        @FailingToStart
        void test_method_whose_recording_fails_to_start() { }

        @WithAPerfIssue
        void test_method_with_a_perf_issue() { }

        @CountingItsCleanings
        void test_method_counting_the_cleanings() { }

        void test_method_without_annotation() { }

    }

    @Test
    void a_failure_to_start_the_recording_should_propagate_unchanged() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_whose_recording_fails_to_start");
        AtomicBoolean testMethodExecuted = new AtomicBoolean();

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodSetting(testMethodExecuted));

        // THEN
        assertThat(throwable).isSameAs(RecorderFailingToStart.START_FAILURE.get());
        assertThat(testMethodExecuted.get()).as("test method executed").isFalse();
        assertThat(RecorderFailingToStart.STOPS.get()).as("stops of the recorder that did not start").isZero();

    }

    @Test
    void a_test_method_without_issue_should_pass() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_counting_the_cleanings");
        AtomicBoolean testMethodExecuted = new AtomicBoolean();

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodSetting(testMethodExecuted));

        // THEN
        assertThat(throwable).isNull();
        assertThat(testMethodExecuted.get()).as("test method executed").isTrue();
        // Released by the extension: in afterEach for a test method, to record its @AfterEach methods too
        assertThat(RecorderCountingItsCleanings.CLEANINGS.get()).as("cleanings of the recorder").isZero();

    }

    @Test
    void a_performance_issue_should_fail_the_test() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_with_a_perf_issue");

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodSetting(new AtomicBoolean()));

        // THEN
        assertThat(throwable).isInstanceOf(AssertionError.class)
                             .hasMessageContaining("a performance-related property is not respected")
                             .hasMessageContaining(PERF_ISSUE);

    }

    @Test
    void a_functional_failure_should_propagate_unchanged_if_there_is_no_performance_issue() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_without_annotation");
        AssertionError functionalFailure = new AssertionError("Functional failure of the test");

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodThrowing(functionalFailure));

        // THEN
        assertThat(throwable).isSameAs(functionalFailure);

    }

    @Test
    void a_functional_failure_with_a_performance_issue_should_be_reported_together() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_with_a_perf_issue");
        AssertionError functionalFailure = new AssertionError("Functional failure of the test");

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodThrowing(functionalFailure));

        // THEN
        assertThat(throwable).isInstanceOf(AssertionError.class)
                             .hasMessageContaining("Performance-related and functional properties not respected")
                             .hasMessageContaining(PERF_ISSUE)
                             .hasMessageContaining("Functional failure of the test");

    }

    @Test
    void an_abort_of_the_test_method_should_stay_an_abort() {

        // GIVEN
        TestExecutionContext testExecutionContext = contextOf("test_method_with_a_perf_issue");
        TestAbortedException abort = new TestAbortedException("Database not available");

        // WHEN
        Throwable throwable = executeAndCheck(testExecutionContext, testMethodThrowing(abort));

        // THEN
        assertThat(throwable).isSameAs(abort);

    }

    private static TestExecutionContext contextOf(String testMethodName) {
        try {
            Method testMethod = TestClass.class.getDeclaredMethod(testMethodName);
            PerfAnnotations perfAnnotations = PerfAnnotations.extractFor(QUICK_PERF_CONFIGS, testMethod);
            return TestExecutionContext.buildInTestJvmFrom(QUICK_PERF_CONFIGS, perfAnnotations.annotationsAppliedInTestJvm());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Throwable executeAndCheck(TestExecutionContext testExecutionContext
                                           , Invocation<Void> testMethodInvocation) {
        return catchThrowable(() -> PerformanceCheck.executeAndCheck(testExecutionContext
                                                                   , QUICK_PERF_CONFIGS
                                                                   , testMethodInvocation));
    }

    private static Invocation<Void> testMethodSetting(AtomicBoolean testMethodExecuted) {
        return () -> {
            testMethodExecuted.set(true);
            return null;
        };
    }

    private static Invocation<Void> testMethodThrowing(Throwable throwable) {
        return () -> {
            throw throwable;
        };
    }

}
