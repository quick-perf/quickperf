/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.config.library;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.WorkingFolder;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.perfrecording.ExtractablePerfRecorderParametersFromAnnotation;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.testlauncher.AllJvmOptions;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.lang.annotation.Annotation;
import java.util.*;

public class SetOfAnnotationConfigs {

    private final Map<Class<? extends Annotation>, Class<? extends RecordablePerformance>> perfRecorderClassByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, ExtractablePerformanceMeasure> perfMeasureExtractorByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, VerifiablePerformanceIssue> perfIssueVerifierByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, Boolean> hasTestToBeLaunchedInASpecificJvmByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, AnnotationToJvmOptionConverter> annotationToJvmParamConverterByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, Class<? extends Annotation>> classOfAnnotationToDisableByAnnotationClass = new HashMap<>();

    private final Map<Class<? extends Annotation>, ExtractablePerfRecorderParametersFromAnnotation> perfRecorderParamsExtractorFromAnnotByAnnotationClass = new HashMap<>();

    public SetOfAnnotationConfigs(Collection<AnnotationConfig> annotationConfigs) {
        for (AnnotationConfig annotationConfig : annotationConfigs) {
            addAnnotationConfig(annotationConfig);
        }
    }

    private void addAnnotationConfig(AnnotationConfig annotationConfig) {
        Class<? extends Annotation> annotationClass = annotationConfig.getClazz();

        Class<? extends Annotation> classOfAnnotationToDisable = annotationConfig.getClassOfAnnotationToDisable();
        if(classOfAnnotationToDisable != null) {
            classOfAnnotationToDisableByAnnotationClass.put(annotationClass, classOfAnnotationToDisable);
        }

        Class<? extends RecordablePerformance> perfRecorderClass = annotationConfig.getPerfRecorderClass();
        perfRecorderClassByAnnotationClass.put(annotationClass, perfRecorderClass);

        ExtractablePerformanceMeasure perfMeasureExtractor = annotationConfig.getPerfMeasureExtractor();
        perfMeasureExtractorByAnnotationClass.put(annotationClass, perfMeasureExtractor);

        VerifiablePerformanceIssue perfIssueVerifier = annotationConfig.getPerfIssueVerifier();
        perfIssueVerifierByAnnotationClass.put(annotationClass, perfIssueVerifier);

        boolean hasTestHasToBeLaunchedInASpecificJvm = annotationConfig.hasTestHasToBeLaunchedInASpecificJvm();
        hasTestToBeLaunchedInASpecificJvmByAnnotationClass.put(annotationClass, hasTestHasToBeLaunchedInASpecificJvm);

        AnnotationToJvmOptionConverter annotationToJvmOptionConverter = annotationConfig.getAnnotationToJvmOptionConverter();
        annotationToJvmParamConverterByAnnotationClass.put(annotationClass, annotationToJvmOptionConverter);

        ExtractablePerfRecorderParametersFromAnnotation perfRecorderParamsExtractorFromAnnot =
                annotationConfig.getPerfRecorderParamsExtractorFromAnnot();
        perfRecorderParamsExtractorFromAnnotByAnnotationClass.put(annotationClass, perfRecorderParamsExtractorFromAnnot);

    }

    public Class<? extends RecordablePerformance> retrievePerfRecorderClassFor(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.annotationType();
        return perfRecorderClassByAnnotationClass.get(clazz);
    }

    public ExtractablePerformanceMeasure retrievePerfMeasureExtractorFor(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.annotationType();
        ExtractablePerformanceMeasure perfMeasureExtractor = perfMeasureExtractorByAnnotationClass.get(clazz);
        if(perfMeasureExtractor == null) {
            return ExtractablePerformanceMeasure.RECORD_IS_PERF_MEASURE;
        }
        return perfMeasureExtractor;
    }

    public VerifiablePerformanceIssue retrievePerfIssuerVerifierFor(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.annotationType();
        VerifiablePerformanceIssue perfIssueVerifier = perfIssueVerifierByAnnotationClass.get(clazz);
        if(perfIssueVerifier == null) {
            return VerifiablePerformanceIssue.NO_VERIFIABLE_PERF_ISSUE;
        }
        return perfIssueVerifier;
    }

    public AllJvmOptions retrieveJvmOptionsFor(Annotation[] annotations, WorkingFolder workingFolder) {
        AllJvmOptions.Builder allJvmsParamsBuilder = new AllJvmOptions.Builder();
        for(Annotation annotation : annotations) {
            Class<? extends Annotation> clazz = annotation.annotationType();
            AnnotationToJvmOptionConverter annotationToJvmOptionConverter = annotationToJvmParamConverterByAnnotationClass.get(clazz);
            if(annotationToJvmOptionConverter != null) {
                @SuppressWarnings("unchecked") //For each annotation a converter is retrieved
                List<JvmOption> jvmOptions = annotationToJvmOptionConverter.convertToJvmOptions(annotation, workingFolder);
                allJvmsParamsBuilder.addOptions(jvmOptions);
            }
        }
        return allJvmsParamsBuilder.build();
    }

    public boolean hasTestMethodToBeLaunchedInASpecificJvmWith(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if(hasTestMethodToBeLaunchedInASpecificJvmWith(annotation)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTestMethodToBeLaunchedInASpecificJvmWith(Annotation annotation) {
        Class<? extends Annotation> clazz = annotation.annotationType();
        Boolean testInNewJvm = hasTestToBeLaunchedInASpecificJvmByAnnotationClass.get(clazz);
        return testInNewJvm == null ? false : testInNewJvm;
    }

    public List<Annotation> removeDisabledAndAndDisablingAnnotationsIn(Collection<Annotation> annotations) {

        List<Annotation> result = new ArrayList<>();

        Collection<Annotation> disablingAnnotations = retrieveDisablingAnnotationsOf(annotations);
        Collection<Class<? extends Annotation>> classesOfDisabledAnnotations = retrieveClassesOfDisabledAnnotations(disablingAnnotations);

        for (Annotation annotation : annotations) {
            if(   !disablingAnnotations.contains(annotation)
               && !classesOfDisabledAnnotations.contains(annotation.annotationType())) {
                result.add(annotation);
            }
        }

        return result;
    }

    private Collection<Annotation> retrieveDisablingAnnotationsOf(Collection<Annotation> annotations) {
        List<Annotation> disablingAnnotations = new ArrayList<>();
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> clazz = annotation.annotationType();
            if(isDisablingAnnotation(clazz)) {
                disablingAnnotations.add(annotation);
            }
        }
        return disablingAnnotations;
    }

    private boolean isDisablingAnnotation(Class<? extends Annotation> clazz) {
        return classOfAnnotationToDisableByAnnotationClass.get(clazz) != null;
    }

    private Collection<Class<? extends Annotation>> retrieveClassesOfDisabledAnnotations(Collection<Annotation> disablingAnnotations) {
        List<Class<? extends Annotation>> classesOfDisabledAnnotations = new ArrayList<>();
        for (Annotation disablingAnnotation : disablingAnnotations) {
            Class<? extends Annotation> clazz = disablingAnnotation.annotationType();
            Class<? extends Annotation> classOfDisabledAnnotation = classOfAnnotationToDisableByAnnotationClass.get(clazz);
            classesOfDisabledAnnotations.add(classOfDisabledAnnotation);
        }
        return classesOfDisabledAnnotations;
    }

    public Collection<Annotation> keepQuickPerfAnnotationsIn(Annotation[] annotations) {
        List<Annotation> quickPerfAnnotations = new ArrayList<>();
        for (Annotation annotation : annotations) {
            if(isCoreAnnotation(annotation) || isQuickPerfPerformanceAnnotation(annotation)) {
                quickPerfAnnotations.add(annotation);
            }
        }
        return quickPerfAnnotations;
    }

    private boolean isCoreAnnotation(Annotation annotation) {
        Package packageOfCoreAnnotations = DisableQuickPerf.class.getPackage();
        return packageOfCoreAnnotations.equals(annotation.annotationType().getPackage());
    }

    private boolean isQuickPerfPerformanceAnnotation(Annotation annotation) {
        Set<Class<? extends Annotation>> classesOfPerformanceAnnotations = perfRecorderClassByAnnotationClass.keySet();
        return classesOfPerformanceAnnotations.contains(annotation.annotationType());
    }

    public ExtractablePerfRecorderParametersFromAnnotation retrievePerfRecorderParamExtractorFor(Annotation perfAnnotation) {
        return perfRecorderParamsExtractorFromAnnotByAnnotationClass.get(perfAnnotation.annotationType());
    }

}
