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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf;

import org.junit.Test;
import org.quickperf.annotation.DisableGlobalAnnotations;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// TODO: REWRITE TESTS
public class AnnotationsMergerTest {

    private final AnnotationsExtractor.AnnotationsMerger annotationsMerger = AnnotationsExtractor.AnnotationsMerger.INSTANCE;

    private @interface PerfAnnotation1 {

        int param();

    }

    private @interface PerfAnnotation2 {}

    private @interface PerfAnnotation3 {}

    private DisableGlobalAnnotations disableGlobalAnnotations = new DisableGlobalAnnotations() {
        @Override
        public String comment() {
            return "";
        }
        @Override
        public Class<? extends Annotation> annotationType() {
            return DisableGlobalAnnotations.class;
        }
    };

    @Test public void
    an_annotation_on_a_method_should_override_an_annotation_on_a_class() {

        // GIVEN
        List<? extends Annotation> globalAnnotations = Collections.emptyList();

        Annotation[] classAnnotations =
                {buildPerfAnnotation1(5)};

        Annotation[] methodAnnotations =
                {buildPerfAnnotation1(10)};


        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                        , classAnnotations
                        , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).hasSize(1);
        PerfAnnotation1 perfAnnotation = (PerfAnnotation1) mergedAnnotations[0];
        assertThat(perfAnnotation.param()).isEqualTo(10);

    }

    private PerfAnnotation1 buildPerfAnnotation1(final int paramValue) {
        return new PerfAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PerfAnnotation1.class;
            }

            @Override
            public int param() {
                return paramValue;
            }
        };
    }

    private PerfAnnotation2 buildPerfAnnotation2() {
        return new PerfAnnotation2() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PerfAnnotation2.class;
            }
        };
    }

    private PerfAnnotation3 buildPerfAnnotation3() {
        return new PerfAnnotation3() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PerfAnnotation3.class;
            }
        };
    }

    @Test public void
    an_annotation_on_a_class_should_override_a_global_annotation() {

        // GIVEN
        List<? extends Annotation> globalAnnotations = Collections.singletonList(buildPerfAnnotation1(15));

        Annotation[] classAnnotations =
                {buildPerfAnnotation1(5)};

        Annotation[] methodAnnotations = {};

        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                        , classAnnotations
                        , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).hasSize(1);
        PerfAnnotation1 perfAnnotation = (PerfAnnotation1) mergedAnnotations[0];
        assertThat(perfAnnotation.param()).isEqualTo(5);

    }

    @Test public void
    an_annotation_on_a_method_should_override_a_global_annotation() {

        // GIVEN
        List<? extends Annotation> globalAnnotations =
                Collections.singletonList(buildPerfAnnotation1(15));

        Annotation[] classAnnotations = {};

        Annotation[] methodAnnotations =
                {buildPerfAnnotation1(10)};

        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                        , classAnnotations
                        , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).hasSize(1);
        PerfAnnotation1 perfAnnotation = (PerfAnnotation1) mergedAnnotations[0];
        assertThat(perfAnnotation.param()).isEqualTo(10);

    }

    @Test public void
    should_keep_global_class_and_method_annotations() {

        // GIVEN
        List<? extends Annotation> globalAnnotations =
                Collections.singletonList(buildPerfAnnotation1(15));

        Annotation[] classAnnotations = {buildPerfAnnotation2()};

        Annotation[] methodAnnotations = {buildPerfAnnotation3()};

        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                        , classAnnotations
                        , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).hasSize(3);

    }

    @Test public void
    global_annotations_should_be_disabled_if_method_is_annotated_with_disable_global_annotations() {

        // GIVEN
        List<? extends Annotation> globalAnnotations =
                Collections.singletonList(buildPerfAnnotation1(5));

        Annotation[] classAnnotations = {};

        Annotation[] methodAnnotations = {disableGlobalAnnotations};

        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                                      , classAnnotations
                                      , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).isEmpty();

    }

    @Test public void
    global_annotations_should_be_disabled_if_class_is_annotated_with_disable_global_annotations() {

        // GIVEN
        List<? extends Annotation> globalAnnotations =
                Collections.singletonList(buildPerfAnnotation1(5));

        Annotation[] classAnnotations = {disableGlobalAnnotations};

        Annotation[] methodAnnotations = {};

        // WHEN
        Annotation[] mergedAnnotations =
                annotationsMerger.merge(globalAnnotations
                                      , classAnnotations
                                      , methodAnnotations);

        // THEN
        assertThat(mergedAnnotations).isEmpty();

    }

}