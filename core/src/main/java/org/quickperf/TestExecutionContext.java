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

import org.quickperf.annotation.DebugQuickPerf;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.DisplayAppliedAnnotations;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.perfrecording.ExecutionOrderOfPerfRecorders;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.testlauncher.AllJvmOptions;
import org.quickperf.testlauncher.JvmOption;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestExecutionContext {

    private WorkingFolder workingFolder;

    private List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod;

    private List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod;

    private boolean testMethodToBeLaunchedInASpecificJvm;

    private AllJvmOptions jvmOptions;

    private Annotation[] perfAnnotations;

    private boolean quickPerfDisabled;

    private boolean quickPerfAnnotationsToBeDisplayed;

    private boolean quickPerfDebugMode;

    private TestExecutionContext() {}

    //TODO: RENAME METHOD AND REFACTOR
    public static TestExecutionContext buildNewJvmFrom(QuickPerfConfigs quickPerfConfigs
                                                     , Method testMethod) {
        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        AnnotationsExtractor annotationsExtractor = AnnotationsExtractor.INSTANCE;

        Annotation[] perfAnnotations = annotationsExtractor.extractAnnotationsFor(testMethod, testAnnotationConfigs);
        boolean isTestMethodToBeLaunchedInASpecificJvm = true;

        TestExecutionContext testExecutionContext = buildFrom(quickPerfConfigs
                                                            , testAnnotationConfigs
                                                            , perfAnnotations
                                                            , isTestMethodToBeLaunchedInASpecificJvm);


        if(testExecutionContext.jvmOptions == null) {
            List<JvmOption> jvmOptionList = new ArrayList<>();
            jvmOptionList.add(new JvmOption("-Xms10m"));
            jvmOptionList.add(new JvmOption("-Xmx10m"));
            testExecutionContext.jvmOptions = new AllJvmOptions.Builder()
                                             .addOptions(jvmOptionList)
                                             .build();
        }

        return testExecutionContext;

    }

    public static TestExecutionContext buildFrom(QuickPerfConfigs quickPerfConfigs
                                               , Method testMethod) {

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        AnnotationsExtractor annotationsExtractor = AnnotationsExtractor.INSTANCE;

        Annotation[] perfAnnotations = annotationsExtractor.extractAnnotationsFor(testMethod, testAnnotationConfigs);
        boolean isTestMethodToBeLaunchedInASpecificJvm = testAnnotationConfigs.hasTestMethodToBeLaunchedInASpecificJvmWith(perfAnnotations);

        return buildFrom(quickPerfConfigs, testAnnotationConfigs, perfAnnotations, isTestMethodToBeLaunchedInASpecificJvm);
    }

    private static TestExecutionContext buildFrom(QuickPerfConfigs quickPerfConfigs
                                               , SetOfAnnotationConfigs testAnnotationConfigs
                                               , Annotation[] perfAnnotations
                                               , boolean isTestMethodToBeLaunchedInASpecificJvm) {

        TestExecutionContext testExecutionContext = new TestExecutionContext();

        if (quickPerfFunctionalitiesAreDisabled(perfAnnotations)) {
            testExecutionContext.quickPerfDisabled = true;
            return testExecutionContext;
        }

        for (Annotation perfAnnotation : perfAnnotations) {
            if (perfAnnotation.annotationType().equals(DisplayAppliedAnnotations.class)) {
                testExecutionContext.quickPerfAnnotationsToBeDisplayed = true;
            }
            if (perfAnnotation.annotationType().equals(DebugQuickPerf.class)) {
                testExecutionContext.quickPerfDebugMode = true;
            }
        }

        testExecutionContext.perfAnnotations = perfAnnotations;

        if(isTestMethodToBeLaunchedInASpecificJvm) {
            testExecutionContext.jvmOptions = testAnnotationConfigs.retrieveJvmOptionsFor(perfAnnotations);
        }

        testExecutionContext.workingFolder = WorkingFolder.createOrRetrieveWorkingFolder(isTestMethodToBeLaunchedInASpecificJvm);
        testExecutionContext.testMethodToBeLaunchedInASpecificJvm = isTestMethodToBeLaunchedInASpecificJvm;


        ExecutionOrderOfPerfRecorders executionOrderOfPerfRecorders = quickPerfConfigs.getExecutionOrderOfPerfRecorders();

        Set<Class<? extends RecordablePerformance>> perfRecorderClasses = testAnnotationConfigs.retrievePerfRecorderClassesFor(perfAnnotations);

        List<RecordablePerformance> perfRecordersToExecute = buildPerfRecorderInstances(perfRecorderClasses);

        testExecutionContext.perfRecordersToExecuteBeforeTestMethod = executionOrderOfPerfRecorders.sortPerfRecordersBeforeTestMethod(perfRecordersToExecute);
        testExecutionContext.perfRecordersToExecuteAfterTestMethod = executionOrderOfPerfRecorders.sortPerfRecordersAfterTestMethod(perfRecordersToExecute);

        return testExecutionContext;
    }

    private static boolean quickPerfFunctionalitiesAreDisabled(Annotation[] perfAnnotations) {
        if(SystemProperties.QUICK_PERF_DISABLED.evaluate()) {
            return true;
        }
        for (Annotation perfAnnotation : perfAnnotations) {
            if (annotationDisablingQuickPerf(perfAnnotation)) {
                return true;
            }
        }
        return false;
    }

    private static boolean annotationDisablingQuickPerf(Annotation perfAnnotation) {
        return     perfAnnotation.annotationType().equals(DisableQuickPerf.class)
                || perfAnnotation.annotationType().equals(FunctionalIteration.class);
    }

    private static List<RecordablePerformance> buildPerfRecorderInstances(Set<Class<? extends RecordablePerformance>> perfRecorderClasses) {
        List<RecordablePerformance> perfRecorders = new ArrayList<>();
        for (Class<? extends RecordablePerformance> perfRecorderClass : perfRecorderClasses) {
            try {
                RecordablePerformance perfRecorder = perfRecorderClass.newInstance();
                perfRecorders.add(perfRecorder);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return perfRecorders;
    }

    public boolean testExecutionUsesTwoJVMs() {
        return testMethodToBeLaunchedInASpecificJvm;
    }

    public boolean testExecutionUsesOneJVM() {
        return !testMethodToBeLaunchedInASpecificJvm;
    }

    public WorkingFolder getWorkingFolder() {
        return workingFolder;
    }

    public AllJvmOptions getJvmOptions() {
        return jvmOptions;
    }

    public List<RecordablePerformance> getPerfRecordersToExecuteBeforeTestMethod() {
        return perfRecordersToExecuteBeforeTestMethod;
    }

    public List<RecordablePerformance> getPerfRecordersToExecuteAfterTestMethod() {
        return perfRecordersToExecuteAfterTestMethod;
    }

    public Annotation[] getPerfAnnotations() {
        return perfAnnotations;
    }

    public boolean isQuickPerfDisabled() {
        return quickPerfDisabled;
    }

    public boolean areQuickPerfAnnotationsToBeDisplayed() {
        return quickPerfAnnotationsToBeDisplayed;
    }

    public boolean isQuickPerfDebugMode() {
        return quickPerfDebugMode;
    }

}

