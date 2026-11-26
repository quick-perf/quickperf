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
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.junit6.FakeQuickPerfConfigs.FakeRecorder;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.junit6.FakeQuickPerfConfigs.quickPerfConfigsOf;

class PerfAnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @interface AppliedInTestJvm { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface NeedingASpecificJvm { }

    @Retention(RetentionPolicy.RUNTIME)
    @interface OtherNeedingASpecificJvm { }

    // Public, as the recorders that TestExecutionContext instantiates from another package: otherwise, building one
    // would fail without incrementing its counter, and the test checking that no recorder is built would still pass
    public static class RecorderOfAnnotationAppliedInTestJvm extends FakeRecorder {

        // The counters of the recorders are static, as TestExecutionContext would build the recorders by reflection,
        // and atomic, so that they stay correct if JUnit executes the tests in parallel
        static final AtomicInteger INSTANCES = new AtomicInteger();

        public RecorderOfAnnotationAppliedInTestJvm() {
            INSTANCES.incrementAndGet();
        }

    }

    public static class RecorderOfAnnotationNeedingASpecificJvm extends FakeRecorder {

        static final AtomicInteger INSTANCES = new AtomicInteger();

        public RecorderOfAnnotationNeedingASpecificJvm() {
            INSTANCES.incrementAndGet();
        }

    }

    private static final QuickPerfConfigs QUICK_PERF_CONFIGS = quickPerfConfigsOf(
              new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationAppliedInTestJvm.class)
                    .build(AppliedInTestJvm.class)
            , new AnnotationConfig.Builder()
                    .perfRecorderClass(RecorderOfAnnotationNeedingASpecificJvm.class)
                    .testHasToBeLaunchedInASpecificJvm()
                    .build(NeedingASpecificJvm.class)
            , new AnnotationConfig.Builder()
                    .testHasToBeLaunchedInASpecificJvm()
                    .build(OtherNeedingASpecificJvm.class));

    @OtherNeedingASpecificJvm
    private static class TestClass {

        @AppliedInTestJvm
        @NeedingASpecificJvm
        void test_method() { }

        @DisableQuickPerf
        @AppliedInTestJvm
        @NeedingASpecificJvm
        void test_method_with_quickperf_disabled() { }

        @FunctionalIteration
        @NeedingASpecificJvm
        void test_method_of_a_functional_iteration() { }

    }

    private static class TestClassWithoutAnnotation {

        void test_method() { }

        @NeedingASpecificJvm
        void test_method_with_an_annotation_of_the_type_of_a_global_one() { }

    }

    @AppliedInTestJvm
    @NeedingASpecificJvm
    private static class GlobalAnnotationsOfTheTests { }

    @Test
    void should_sort_the_annotations_applied_in_the_test_jvm_and_those_needing_a_specific_jvm() {

        // WHEN
        PerfAnnotations perfAnnotations = extractFor(methodOf(TestClass.class, "test_method"));

        // THEN
        assertThat(perfAnnotations.quickPerfDisabled()).isFalse();
        assertThat(typesOf(perfAnnotations.annotationsAppliedInTestJvm())).containsExactly(AppliedInTestJvm.class);
        assertThat(typesOf(perfAnnotations.testAnnotationsNeedingASpecificJvm()))
                .containsOnly(NeedingASpecificJvm.class, OtherNeedingASpecificJvm.class);
        assertThat(perfAnnotations.globalAnnotationsNeedingASpecificJvm()).isEmpty();

    }

    @Test
    void should_find_no_annotation_needing_a_specific_jvm_if_there_is_none() {

        // WHEN
        PerfAnnotations perfAnnotations = extractFor(methodOf(TestClassWithoutAnnotation.class, "test_method"));

        // THEN
        assertThat(perfAnnotations.annotationsAppliedInTestJvm()).isEmpty();
        assertThat(perfAnnotations.testAnnotationsNeedingASpecificJvm()).isEmpty();
        assertThat(perfAnnotations.globalAnnotationsNeedingASpecificJvm()).isEmpty();

    }

    // The tests of this module have no global annotations: the merged annotations are given
    @Test
    void should_sort_the_global_annotations_needing_a_specific_jvm_apart() {

        // GIVEN
        Method testMethod = methodOf(TestClassWithoutAnnotation.class, "test_method");
        Annotation[] mergedAnnotations = GlobalAnnotationsOfTheTests.class.getAnnotations();

        // WHEN
        PerfAnnotations perfAnnotations = sort(mergedAnnotations, testMethod);

        // THEN
        assertThat(typesOf(perfAnnotations.annotationsAppliedInTestJvm())).containsExactly(AppliedInTestJvm.class);
        assertThat(perfAnnotations.testAnnotationsNeedingASpecificJvm()).isEmpty();
        assertThat(typesOf(perfAnnotations.globalAnnotationsNeedingASpecificJvm()))
                .containsExactly(NeedingASpecificJvm.class);

    }

    @Test
    void an_annotation_of_the_test_method_replacing_a_global_annotation_should_be_an_annotation_of_the_test() {

        // GIVEN
        Method testMethod = methodOf(TestClassWithoutAnnotation.class
                                   , "test_method_with_an_annotation_of_the_type_of_a_global_one");
        // The annotation of the test method replaced the global annotation of the same type
        Annotation[] mergedAnnotations = { testMethod.getAnnotation(NeedingASpecificJvm.class)
                                         , GlobalAnnotationsOfTheTests.class.getAnnotation(AppliedInTestJvm.class)};

        // WHEN
        PerfAnnotations perfAnnotations = sort(mergedAnnotations, testMethod);

        // THEN
        assertThat(typesOf(perfAnnotations.testAnnotationsNeedingASpecificJvm()))
                .containsExactly(NeedingASpecificJvm.class);
        assertThat(perfAnnotations.globalAnnotationsNeedingASpecificJvm()).isEmpty();

    }

    @Test
    void should_find_no_annotation_needing_a_specific_jvm_if_quickperf_is_disabled() {

        // WHEN
        PerfAnnotations perfAnnotations = extractFor(methodOf(TestClass.class, "test_method_with_quickperf_disabled"));

        // THEN
        assertThat(perfAnnotations.quickPerfDisabled()).isTrue();
        assertThat(typesOf(perfAnnotations.annotationsAppliedInTestJvm()))
                .as("annotations of the disabled context")
                .contains(DisableQuickPerf.class);
        assertThat(perfAnnotations.testAnnotationsNeedingASpecificJvm()).isEmpty();
        assertThat(perfAnnotations.globalAnnotationsNeedingASpecificJvm()).isEmpty();

    }

    @Test
    void a_functional_iteration_should_disable_quickperf() {

        // WHEN
        PerfAnnotations perfAnnotations = extractFor(methodOf(TestClass.class, "test_method_of_a_functional_iteration"));

        // THEN
        assertThat(perfAnnotations.quickPerfDisabled()).isTrue();
        assertThat(perfAnnotations.testAnnotationsNeedingASpecificJvm()).isEmpty();

    }

    @Test
    void should_build_no_recorder() {

        // WHEN
        extractFor(methodOf(TestClass.class, "test_method"));
        sort(GlobalAnnotationsOfTheTests.class.getAnnotations(), methodOf(TestClassWithoutAnnotation.class, "test_method"));

        // THEN
        assertThat(RecorderOfAnnotationAppliedInTestJvm.INSTANCES.get()).isZero();
        assertThat(RecorderOfAnnotationNeedingASpecificJvm.INSTANCES.get()).isZero();

    }

    private static PerfAnnotations extractFor(Method testMethod) {
        return PerfAnnotations.extractFor(QUICK_PERF_CONFIGS, testMethod);
    }

    private static PerfAnnotations sort(Annotation[] mergedAnnotations, Method testMethod) {
        return PerfAnnotations.sort(mergedAnnotations, testMethod, QUICK_PERF_CONFIGS.getTestAnnotationConfigs());
    }

    private static Method methodOf(Class<?> testClass, String testMethodName) {
        try {
            return testClass.getDeclaredMethod(testMethodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Class<?>[] typesOf(Annotation[] annotations) {
        return typesOf(Arrays.asList(annotations));
    }

    private static Class<?>[] typesOf(Collection<Annotation> annotations) {
        return annotations.stream().map(Annotation::annotationType).toArray(Class<?>[]::new);
    }

}
