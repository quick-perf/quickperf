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

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationFormatterTest {

    private @interface PerfAnnotation1 {
    }

    private @interface PerfAnnotation2 {

        int param1();

        int param2();

    }

    @Test
    public void
    should_format_annotation() {

        // GIVEN
        AnnotationFormatter annotationFormatter = AnnotationFormatter.INSTANCE;

        PerfAnnotation1 perfAnnotation1 = new PerfAnnotation1() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PerfAnnotation1.class;
            }
        };

        // WHEN
        String formattedAnnotation = annotationFormatter.format(perfAnnotation1);

        // THEN
        assertThat(formattedAnnotation).isEqualTo("@PerfAnnotation1");

    }

    @Test
    public void
    should_format_annotation_with_parameters() {

        // GIVEN
        AnnotationFormatter annotationFormatter = AnnotationFormatter.INSTANCE;

        PerfAnnotation2 perfAnnotation2 = new PerfAnnotation2() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return PerfAnnotation2.class;
            }

            @Override
            public int param1() {
                return 10;
            }

            @Override
            public int param2() {
                return 20;
            }
        };

        // WHEN
        String formattedAnnotation = annotationFormatter.format(perfAnnotation2);

        // THEN
        assertThat(formattedAnnotation).contains("@PerfAnnotation2(")
                                       .contains("param1=10")
                                       .contains("param2=20")
                                       .contains(")")
                                       ;
    }

}