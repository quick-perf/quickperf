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
package org.quickperf;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.Test;
import org.quickperf.FakeQuickPerfConfigs.FakeRecorder;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.perfrecording.ExtractablePerfRecorderParametersFromAnnotation;
import org.quickperf.perfrecording.PerfRecorderParameters;
import org.quickperf.perfrecording.RecordablePerformance;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.quickperf.FakeQuickPerfConfigs.quickPerfConfigsOf;

public class TestExecutionContextTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface AppliedInTestJvm { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NeedingASpecificJvm { }

    static class RecorderOfAnnotationAppliedInTestJvm extends FakeRecorder { }

    static class RecorderOfAnnotationNeedingASpecificJvm extends FakeRecorder { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface RegisteringItsRecorder { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface WithAnInvalidValue { }

    private static final String INVALID_VALUE_MESSAGE = "Invalid value of the annotation";

    // As DisplaySqlRecorder, registers itself when it is built
    static class RecorderRegisteringItself extends FakeRecorder {

        // Static, as TestExecutionContext builds the recorders by reflection, and atomic, as Surefire executes the
        // tests of this module on parallel threads. A recorder constructor throwing an exception is swallowed: the
        // counter tells whether it was called.
        static final AtomicInteger INSTANCES = new AtomicInteger();

        static final Set<RecorderRegisteringItself> REGISTERED =
                Collections.newSetFromMap(new ConcurrentHashMap<RecorderRegisteringItself, Boolean>());

        public RecorderRegisteringItself() {
            INSTANCES.incrementAndGet();
            REGISTERED.add(this);
        }

        @Override
        public void cleanResources() {
            REGISTERED.remove(this);
        }

    }

    static class RecorderOfAnnotationWithAnInvalidValue extends FakeRecorder { }

    // As the extractor of the parameters of @ProfileConnection with a stack depth below -1
    private static final ExtractablePerfRecorderParametersFromAnnotation<WithAnInvalidValue, PerfRecorderParameters>
            REJECTING_PARAMETERS_EXTRACTOR =
            new ExtractablePerfRecorderParametersFromAnnotation<WithAnInvalidValue, PerfRecorderParameters>() {
                @Override
                public PerfRecorderParameters extractFrom(WithAnInvalidValue annotation) {
                    throw new IllegalArgumentException(INVALID_VALUE_MESSAGE);
                }
            };

    private static final QuickPerfConfigs QUICK_PERF_CONFIGS = quickPerfConfigsOf(
              new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationAppliedInTestJvm.class)
                    .build(AppliedInTestJvm.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationNeedingASpecificJvm.class)
                    .testHasToBeLaunchedInASpecificJvm()
                    .build(NeedingASpecificJvm.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderRegisteringItself.class)
                    .build(RegisteringItsRecorder.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationWithAnInvalidValue.class)
                    .perfRecorderParamsExtractorFromAnnot(REJECTING_PARAMETERS_EXTRACTOR)
                    .build(WithAnInvalidValue.class));

    private static class TestClass {

        @AppliedInTestJvm
        @NeedingASpecificJvm
        public void test_method() { }

        @DisableQuickPerf
        @AppliedInTestJvm
        public void test_method_with_quickperf_disabled() { }

    }

    // The adapter leaves out the annotation of the test method needing a specific JVM
    @Test public void
    a_context_built_in_the_test_jvm_should_apply_only_the_given_annotations() {

        // GIVEN
        Annotation[] perfAnnotations = {methodOf(TestClass.class, "test_method").getAnnotation(AppliedInTestJvm.class)};

        // WHEN
        TestExecutionContext testExecutionContext = TestExecutionContext.buildInTestJvmFrom(QUICK_PERF_CONFIGS
                                                                                          , perfAnnotations);

        // THEN
        assertThat(typesOf(testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod()))
                .containsExactly(RecorderOfAnnotationAppliedInTestJvm.class);
        assertThat(typesOf(testExecutionContext.getPerfRecordersToExecuteAfterTestMethod()))
                .containsExactly(RecorderOfAnnotationAppliedInTestJvm.class);
        assertThat(testExecutionContext.getPerfAnnotations()).containsExactly(perfAnnotations);
        assertThat(testExecutionContext.testExecutionUsesOneJVM()).isTrue();
        assertThat(testExecutionContext.getJvmOptions()).isNull();
        assertThat(testExecutionContext.getWorkingFolder().getPath())
                .as("path of the working folder, empty when there is none")
                .isEmpty();
        assertThat(testExecutionContext.isQuickPerfDisabled()).isFalse();

    }

    @Test public void
    a_context_built_in_the_test_jvm_should_be_disabled_if_quickperf_is_disabled() {

        // GIVEN
        Annotation[] perfAnnotations = methodOf(TestClass.class, "test_method_with_quickperf_disabled").getAnnotations();

        // WHEN
        TestExecutionContext testExecutionContext = TestExecutionContext.buildInTestJvmFrom(QUICK_PERF_CONFIGS
                                                                                          , perfAnnotations);

        // THEN
        assertThat(testExecutionContext.isQuickPerfDisabled()).isTrue();
        assertThat(testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod()).isEmpty();

    }

    // The annotations of the test method come before those of its class: the recorder registering itself is built first
    @WithAnInvalidValue
    private static class TestClassWithAnInvalidAnnotationValue {

        @RegisteringItsRecorder
        public void test_method() { }

    }

    @Test public void
    should_release_the_recorders_already_built_when_building_the_context_fails() {

        // GIVEN
        final Method testMethod = methodOf(TestClassWithAnInvalidAnnotationValue.class, "test_method");

        // WHEN
        Throwable buildFailure = catchThrowable(new ThrowingCallable() {
            @Override
            public void call() {
                TestExecutionContext.buildFrom(QUICK_PERF_CONFIGS, testMethod, 0);
            }
        });

        // THEN
        assertThat(buildFailure).isInstanceOf(IllegalArgumentException.class)
                                .hasMessage(INVALID_VALUE_MESSAGE);
        assertThat(RecorderRegisteringItself.INSTANCES.get())
                .as("instances of the recorder registering itself")
                .isOne();
        assertThat(RecorderRegisteringItself.REGISTERED)
                .as("recorders still registered")
                .isEmpty();

    }

    private static Method methodOf(Class<?> testClass, String testMethodName) {
        try {
            return testClass.getDeclaredMethod(testMethodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Class<?>> typesOf(List<RecordablePerformance> perfRecorders) {
        List<Class<?>> types = new ArrayList<>();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            types.add(perfRecorder.getClass());
        }
        return types;
    }

}
