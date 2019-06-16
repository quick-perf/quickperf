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

package org.quickperf.config.library;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;

import java.lang.annotation.Annotation;

public class AnnotationConfig {

    private AnnotationConfig() { }

    private Class<? extends Annotation> clazz;

    private Class<? extends RecordablePerformance> perfRecorderClass;

    private ExtractablePerformanceMeasure perfMeasureExtractor;

    private VerifiablePerformanceIssue perfIssueVerifier;

    private boolean testHasToBeLaunchedInASpecificJvm;

    private Class<? extends Annotation> classOfAnnotationToDisable;

    private AnnotationToJvmOptionConverter annotationToJvmOptionConverter;

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

    Class<? extends Annotation> getClassOfAnnotationToDisable() {
        return classOfAnnotationToDisable;
    }

    public static class Builder {

        private Class<? extends RecordablePerformance> perfRecorderClass;

        private VerifiablePerformanceIssue perfIssueVerifier;

        private boolean testHasToBeLaunchedInASpecificJvm;

        private Class<? extends Annotation> classOfAnnotationToDisable;

        private AnnotationToJvmOptionConverter annotationToJvmOptionConverter
                = AnnotationToJvmOptionConverter.NONE;

        private ExtractablePerformanceMeasure perfMeasureExtractor;

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

        public Builder cancelBehaviorOf(Class<? extends Annotation> annotationToDisable) {
            this.classOfAnnotationToDisable = annotationToDisable;
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
            annotationConfig.classOfAnnotationToDisable = classOfAnnotationToDisable;
            return annotationConfig;
        }

    }

}
