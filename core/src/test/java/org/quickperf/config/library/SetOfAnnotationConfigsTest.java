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
package org.quickperf.config.library;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SetOfAnnotationConfigsTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotation1 {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotation2 {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotation3 {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface CancelingAnnotation {}

    @PerfAnnotation1
    @PerfAnnotation2
    @PerfAnnotation3
    @CancelingAnnotation
    private static class AnnotatedClass {}

    @Test public void
    an_annotation_can_cancel_the_behavior_of_several_annotations() {

        // GIVEN
        AnnotationConfig cancelingAnnotationConfig = new AnnotationConfig.Builder()
                .cancelBehaviorOf(PerfAnnotation1.class)
                .cancelBehaviorOf(PerfAnnotation2.class)
                .build(CancelingAnnotation.class);
        SetOfAnnotationConfigs setOfAnnotationConfigs =
                new SetOfAnnotationConfigs(Arrays.asList(cancelingAnnotationConfig));

        List<Annotation> annotations = Arrays.asList(AnnotatedClass.class.getAnnotations());

        // WHEN
        List<Annotation> result = setOfAnnotationConfigs.removeDisabledAndAndDisablingAnnotationsIn(annotations);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).annotationType()).isEqualTo(PerfAnnotation3.class);

    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface ReplacingAnnotation {}

    @PerfAnnotation1
    @ReplacingAnnotation
    private static class ClassWithReplacedAndReplacingAnnotations {}

    @PerfAnnotation1
    private static class ClassWithReplacedAnnotation {}

    @Test public void
    a_replaced_annotation_should_be_removed_if_the_replacing_annotation_is_present() {

        // GIVEN
        AnnotationConfig replacingAnnotationConfig = new AnnotationConfig.Builder()
                .replace(PerfAnnotation1.class)
                .build(ReplacingAnnotation.class);
        SetOfAnnotationConfigs setOfAnnotationConfigs =
                new SetOfAnnotationConfigs(Arrays.asList(replacingAnnotationConfig));

        List<Annotation> annotations = Arrays.asList(ClassWithReplacedAndReplacingAnnotations.class.getAnnotations());

        // WHEN
        List<Annotation> result = setOfAnnotationConfigs.removeDisabledAndAndDisablingAnnotationsIn(annotations);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).annotationType()).isEqualTo(ReplacingAnnotation.class);

    }

    @Test public void
    a_replaced_annotation_should_be_kept_if_the_replacing_annotation_is_absent() {

        // GIVEN
        AnnotationConfig replacingAnnotationConfig = new AnnotationConfig.Builder()
                .replace(PerfAnnotation1.class)
                .build(ReplacingAnnotation.class);
        SetOfAnnotationConfigs setOfAnnotationConfigs =
                new SetOfAnnotationConfigs(Arrays.asList(replacingAnnotationConfig));

        List<Annotation> annotations = Arrays.asList(ClassWithReplacedAnnotation.class.getAnnotations());

        // WHEN
        List<Annotation> result = setOfAnnotationConfigs.removeDisabledAndAndDisablingAnnotationsIn(annotations);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).annotationType()).isEqualTo(PerfAnnotation1.class);

    }

    @PerfAnnotation1
    @ReplacingAnnotation
    @CancelingAnnotation
    private static class ClassWithReplacedAnnotationAndCanceledReplacingAnnotation {}

    @Test public void
    a_replaced_annotation_should_be_kept_if_the_replacing_annotation_is_canceled() {

        // GIVEN
        AnnotationConfig replacingAnnotationConfig = new AnnotationConfig.Builder()
                .replace(PerfAnnotation1.class)
                .build(ReplacingAnnotation.class);
        AnnotationConfig cancelingAnnotationConfig = new AnnotationConfig.Builder()
                .cancelBehaviorOf(ReplacingAnnotation.class)
                .build(CancelingAnnotation.class);
        SetOfAnnotationConfigs setOfAnnotationConfigs =
                new SetOfAnnotationConfigs(Arrays.asList(replacingAnnotationConfig, cancelingAnnotationConfig));

        List<Annotation> annotations = Arrays.asList(ClassWithReplacedAnnotationAndCanceledReplacingAnnotation.class.getAnnotations());

        // WHEN
        List<Annotation> result = setOfAnnotationConfigs.removeDisabledAndAndDisablingAnnotationsIn(annotations);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).annotationType()).isEqualTo(PerfAnnotation1.class);

    }

}
