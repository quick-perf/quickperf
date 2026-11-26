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

import org.quickperf.AnnotationsExtractor;
import org.quickperf.SystemProperties;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The QuickPerf annotations of a test method, sorted without building any recorder: the extension can reject the test
 * before anything is built.
 *
 * @param quickPerfDisabled with <code>@DisableQuickPerf</code>, <code>@FunctionalIteration</code>, or
 * <code>-DdisableQuickPerf=true</code>. The lists of annotations needing a specific JVM are then empty.
 * @param annotationsAppliedInTestJvm the annotations to build the context from. All the annotations if QuickPerf is
 * disabled: the context is then disabled too.
 * @param testAnnotationsNeedingASpecificJvm the annotations needing a specific JVM that are on the test method or on its
 * class
 * @param globalAnnotationsNeedingASpecificJvm the global annotations needing a specific JVM
 */
record PerfAnnotations(boolean quickPerfDisabled
                     , Annotation[] annotationsAppliedInTestJvm
                     , List<Annotation> testAnnotationsNeedingASpecificJvm
                     , List<Annotation> globalAnnotationsNeedingASpecificJvm) {

    static PerfAnnotations extractFor(QuickPerfConfigs quickPerfConfigs, Method testMethod) {
        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        // With every annotation config, so that the merge, cancel and replace rules are those of the extensions starting
        // a specific JVM. A set of configs without the annotations needing a specific JVM would drop them silently.
        Annotation[] perfAnnotations = AnnotationsExtractor.INSTANCE.extractAnnotationsFor(testMethod, testAnnotationConfigs);
        return sort(perfAnnotations, testMethod, testAnnotationConfigs);
    }

    /**
     * @param perfAnnotations the QuickPerf annotations of the test method, of its class, and the global ones, merged
     */
    static PerfAnnotations sort(Annotation[] perfAnnotations
                              , Method testMethod
                              , SetOfAnnotationConfigs testAnnotationConfigs) {

        if (quickPerfIsDisabled(perfAnnotations)) {
            return new PerfAnnotations(true, perfAnnotations, List.of(), List.of());
        }

        List<Annotation> annotationsAppliedInTestJvm = new ArrayList<>();
        List<Annotation> testAnnotationsNeedingASpecificJvm = new ArrayList<>();
        List<Annotation> globalAnnotationsNeedingASpecificJvm = new ArrayList<>();
        for (Annotation perfAnnotation : perfAnnotations) {
            if (!testAnnotationConfigs.hasTestMethodToBeLaunchedInASpecificJvmWith(new Annotation[] {perfAnnotation})) {
                annotationsAppliedInTestJvm.add(perfAnnotation);
            } else if (isOnTheTestMethodOrItsClass(perfAnnotation, testMethod)) {
                testAnnotationsNeedingASpecificJvm.add(perfAnnotation);
            } else {
                globalAnnotationsNeedingASpecificJvm.add(perfAnnotation);
            }
        }

        return new PerfAnnotations(false
                                 , annotationsAppliedInTestJvm.toArray(new Annotation[0])
                                 , List.copyOf(testAnnotationsNeedingASpecificJvm)
                                 , List.copyOf(globalAnnotationsNeedingASpecificJvm));

    }

    // As TestExecutionContext, which builds a disabled context from these annotations
    private static boolean quickPerfIsDisabled(Annotation[] perfAnnotations) {
        return    SystemProperties.QUICK_PERF_DISABLED.evaluate()
               || Arrays.stream(perfAnnotations)
                        .map(Annotation::annotationType)
                        .anyMatch(type -> type == DisableQuickPerf.class || type == FunctionalIteration.class);
    }

    // As AnnotationsExtractor, which reads the annotations of the test method and of its declaring class: an annotation of
    // one of them replaces a global annotation of the same type
    private static boolean isOnTheTestMethodOrItsClass(Annotation perfAnnotation, Method testMethod) {
        Class<? extends Annotation> annotationType = perfAnnotation.annotationType();
        return    testMethod.isAnnotationPresent(annotationType)
               || testMethod.getDeclaringClass().isAnnotationPresent(annotationType);
    }

}
