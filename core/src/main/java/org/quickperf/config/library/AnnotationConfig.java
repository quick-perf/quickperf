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

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.perfrecording.ExtractablePerfRecorderParametersFromAnnotation;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AnnotationConfig {

    private AnnotationConfig() { }

    private Class<? extends Annotation> clazz;

    private Class<? extends RecordablePerformance> perfRecorderClass;

    private ExtractablePerformanceMeasure perfMeasureExtractor;

    private VerifiablePerformanceIssue perfIssueVerifier;

    private boolean testHasToBeLaunchedInASpecificJvm;

    private List<Class<? extends Annotation>> classesOfAnnotationsToDisable;

    private Class<? extends Annotation> classOfReplacedAnnotation;

    private AnnotationToJvmOptionConverter annotationToJvmOptionConverter;

    private ExtractablePerfRecorderParametersFromAnnotation perfRecorderParamsExtractorFromAnnot;

    public Class<? extends Annotation> getClazz() {
        return clazz;
    }

    public Class<? extends RecordablePerformance> getPerfRecorderClass() {
        return perfRecorderClass;
    }

    ExtractablePerformanceMeasure getPerfMeasureExtractor() {
        return perfMeasureExtractor;
    }

    VerifiablePerformanceIssue getPerfIssueVerifier() {
        return perfIssueVerifier;
    }

    boolean hasTestHasToBeLaunchedInASpecificJvm() {
        return testHasToBeLaunchedInASpecificJvm;
    }

    AnnotationToJvmOptionConverter getAnnotationToJvmOptionConverter() {
        return annotationToJvmOptionConverter;
    }

    public ExtractablePerfRecorderParametersFromAnnotation getPerfRecorderParamsExtractorFromAnnot() {
        return perfRecorderParamsExtractorFromAnnot;
    }

    List<Class<? extends Annotation>> getClassesOfAnnotationsToDisable() {
        return classesOfAnnotationsToDisable;
    }

    Class<? extends Annotation> getClassOfReplacedAnnotation() {
        return classOfReplacedAnnotation;
    }

    public static class Builder {

        private Class<? extends RecordablePerformance> perfRecorderClass;

        private VerifiablePerformanceIssue perfIssueVerifier;

        private boolean testHasToBeLaunchedInASpecificJvm;

        private final List<Class<? extends Annotation>> classesOfAnnotationsToDisable = new ArrayList<>();

        private Class<? extends Annotation> classOfReplacedAnnotation;

        private AnnotationToJvmOptionConverter annotationToJvmOptionConverter
                = AnnotationToJvmOptionConverter.NONE;

        private ExtractablePerformanceMeasure perfMeasureExtractor;

        private ExtractablePerfRecorderParametersFromAnnotation perfRecorderParamsExtractorFromAnnot;

        public Builder perfRecorderClass(Class<? extends RecordablePerformance> recordablePerformance) {
            this.perfRecorderClass = recordablePerformance;
            return this;
        }

        public Builder perfMeasureExtractor(ExtractablePerformanceMeasure perfMeasureExtractor) {
            this.perfMeasureExtractor = perfMeasureExtractor;
            return this;
        }

        public Builder perfIssueVerifier(VerifiablePerformanceIssue perfVerifier) {
            this.perfIssueVerifier = perfVerifier;
            return this;
        }

        public Builder testHasToBeLaunchedInASpecificJvm() {
            this.testHasToBeLaunchedInASpecificJvm = true;
            this.annotationToJvmOptionConverter = AnnotationToJvmOptionConverter.NONE;
            return this;
        }

        public Builder testHasToBeLaunchedInASpecificJvm(AnnotationToJvmOptionConverter annotationToJvmOptionConverter) {
            this.testHasToBeLaunchedInASpecificJvm = true;
            this.annotationToJvmOptionConverter = annotationToJvmOptionConverter;
            return this;
        }

        /**
         * Can be called several times to cancel the behavior of several annotations.
         */
        public Builder cancelBehaviorOf(Class<? extends Annotation> annotationToDisable) {
            this.classesOfAnnotationsToDisable.add(annotationToDisable);
            return this;
        }

        /**
         * Declares that the annotation replaces another one (typically a deprecated one). If both annotations apply to
         * a test, only the replacing annotation is kept, so that the same verification is not done twice.
         */
        public Builder replace(Class<? extends Annotation> replacedAnnotation) {
            this.classOfReplacedAnnotation = replacedAnnotation;
            return this;
        }

        public Builder perfRecorderParamsExtractorFromAnnot(ExtractablePerfRecorderParametersFromAnnotation perfRecorderParamsExtractor) {
            this.perfRecorderParamsExtractorFromAnnot = perfRecorderParamsExtractor;
            return this;
        }

        public AnnotationConfig build(Class<? extends Annotation> clazz) {
            AnnotationConfig annotationConfig = new AnnotationConfig();
            annotationConfig.clazz = clazz;
            annotationConfig.perfRecorderClass = perfRecorderClass;
            if(perfMeasureExtractor == null) {
                annotationConfig.perfMeasureExtractor = ExtractablePerformanceMeasure.RECORD_IS_PERF_MEASURE;
            } else {
                annotationConfig.perfMeasureExtractor = perfMeasureExtractor;
            }
            if(perfIssueVerifier == null) {
                annotationConfig.perfIssueVerifier = VerifiablePerformanceIssue.NO_VERIFIABLE_PERF_ISSUE;
            } else {
                annotationConfig.perfIssueVerifier = perfIssueVerifier;

            }
            annotationConfig.testHasToBeLaunchedInASpecificJvm = testHasToBeLaunchedInASpecificJvm;
            annotationConfig.annotationToJvmOptionConverter = annotationToJvmOptionConverter;
            annotationConfig.classesOfAnnotationsToDisable =
                    Collections.unmodifiableList(new ArrayList<>(classesOfAnnotationsToDisable));
            annotationConfig.classOfReplacedAnnotation = classOfReplacedAnnotation;
            annotationConfig.perfRecorderParamsExtractorFromAnnot = perfRecorderParamsExtractorFromAnnot;
            return annotationConfig;
        }

    }

}
