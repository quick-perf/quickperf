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

package org.quickperf;

import org.quickperf.annotation.DebugQuickPerf;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.DisplayAppliedAnnotations;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.perfrecording.ExecutionOrderOfPerfRecorders;
import org.quickperf.perfrecording.ExtractablePerfRecorderParametersFromAnnotation;
import org.quickperf.perfrecording.PerfRecorderParameters;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.testlauncher.AllJvmOptions;
import org.quickperf.testlauncher.JvmOption;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class TestExecutionContext {

    private WorkingFolder workingFolder;

    private List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod = Collections.emptyList();

    private List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod = Collections.emptyList();

    private boolean testMethodToBeLaunchedInASpecificJvm;

    private AllJvmOptions jvmOptions;

    private Annotation[] perfAnnotations;

    private boolean quickPerfDisabled;

    private boolean quickPerfAnnotationsToBeDisplayed;

    private boolean quickPerfDebugMode;

    private int runnerAllocationOffset;

    private TestExecutionContext() {}

    // TODO: RENAME METHOD AND REFACTOR
    // Used by QuickPerfSpringRunner
    public static TestExecutionContext buildNewJvmFrom(QuickPerfConfigs quickPerfConfigs
                                                     , Method testMethod) {
        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        AnnotationsExtractor annotationsExtractor = AnnotationsExtractor.INSTANCE;

        Annotation[] perfAnnotations = annotationsExtractor.extractAnnotationsFor(testMethod, testAnnotationConfigs);
        boolean isTestMethodToBeLaunchedInASpecificJvm = true;

        int runnerAllocationOffset = 0;
        TestExecutionContext testExecutionContext = buildFrom(quickPerfConfigs
                                                            , testAnnotationConfigs
                                                            , perfAnnotations
                                                            , isTestMethodToBeLaunchedInASpecificJvm
                                                            , runnerAllocationOffset);


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

    public static TestExecutionContext buildFrom( QuickPerfConfigs quickPerfConfigs
                                                , Method testMethod
                                                , int runnerAllocationOffset) {

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        AnnotationsExtractor annotationsExtractor = AnnotationsExtractor.INSTANCE;

        Annotation[] perfAnnotations = annotationsExtractor.extractAnnotationsFor(testMethod, testAnnotationConfigs);
        boolean isTestMethodToBeLaunchedInASpecificJvm = testAnnotationConfigs.hasTestMethodToBeLaunchedInASpecificJvmWith(perfAnnotations);

        return buildFrom(quickPerfConfigs
                       , testAnnotationConfigs
                       , perfAnnotations
                       , isTestMethodToBeLaunchedInASpecificJvm
                       , runnerAllocationOffset);
    }

    private static TestExecutionContext buildFrom(QuickPerfConfigs quickPerfConfigs
                                                , SetOfAnnotationConfigs testAnnotationConfigs
                                                , Annotation[] perfAnnotations
                                                , boolean isTestMethodToBeLaunchedInASpecificJvm
                                                , int runnerAllocationOffset) {

        TestExecutionContext testExecutionContext = new TestExecutionContext();

        if (quickPerfIsDisabled(perfAnnotations)) {
            testExecutionContext.quickPerfDisabled = true;
            return testExecutionContext;
        }

        testExecutionContext.quickPerfAnnotationsToBeDisplayed
                = haveQuickPerfAnnotationsToBeDisplayed(perfAnnotations);

        testExecutionContext.quickPerfDebugMode = isQuickPerfDebugMode(perfAnnotations);

        testExecutionContext.perfAnnotations = perfAnnotations;

        WorkingFolder workingFolder = WorkingFolder.createOrRetrieveWorkingFolder(isTestMethodToBeLaunchedInASpecificJvm);
        testExecutionContext.workingFolder = workingFolder;

        testExecutionContext.testMethodToBeLaunchedInASpecificJvm = isTestMethodToBeLaunchedInASpecificJvm;

        Boolean testCodeIsExecutingInNewJvm = SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();

        if (isTestMethodToBeLaunchedInASpecificJvm && !testCodeIsExecutingInNewJvm) {
            testExecutionContext.jvmOptions =
                    testAnnotationConfigs.retrieveJvmOptionsFor(perfAnnotations, workingFolder);
        }

        if(testCodeIsExecutingInNewJvm) {
            testExecutionContext.runnerAllocationOffset = runnerAllocationOffset;
        }

        List<RecordablePerformance> perfRecordersToExecute = buildPerfRecordersToExecute(testAnnotationConfigs, perfAnnotations);

        ExecutionOrderOfPerfRecorders executionOrderOfPerfRecorders = quickPerfConfigs.getExecutionOrderOfPerfRecorders();

        testExecutionContext.perfRecordersToExecuteBeforeTestMethod = executionOrderOfPerfRecorders.sortPerfRecordersBeforeTestMethod(perfRecordersToExecute);
        testExecutionContext.perfRecordersToExecuteAfterTestMethod = executionOrderOfPerfRecorders.sortPerfRecordersAfterTestMethod(perfRecordersToExecute);

        return testExecutionContext;
    }

    private static List<RecordablePerformance> buildPerfRecordersToExecute(SetOfAnnotationConfigs testAnnotationConfigs, Annotation[] perfAnnotations) {
        List<RecordablePerformance> perfRecordersToExecute = new ArrayList<>();
        Set<Class<? extends RecordablePerformance>> perfRecorderClasses = new HashSet<>();
        for (Annotation perfAnnotation : perfAnnotations) {
            Class<? extends RecordablePerformance> perfRecorderClass = testAnnotationConfigs.retrievePerfRecorderClassFor(perfAnnotation);
            if(perfRecorderClass!= null && !perfRecorderClasses.contains(perfRecorderClass)) {
                RecordablePerformance perfRecorder = buildPerfRecorder(perfAnnotation
                                                                     , perfRecorderClass
                                                                     , testAnnotationConfigs
                                                                      );
                perfRecordersToExecute.add(perfRecorder);
            }
        }
        return perfRecordersToExecute;
    }

    @SuppressWarnings("unchecked")
    private static RecordablePerformance buildPerfRecorder(Annotation perfAnnotation
                                                         , Class<? extends RecordablePerformance> perfRecorderClass
                                                         , SetOfAnnotationConfigs testAnnotationConfigs) {
        ExtractablePerfRecorderParametersFromAnnotation perfRecorderParamExtractor
                = testAnnotationConfigs.retrievePerfRecorderParamExtractorFor(perfAnnotation);
        if(perfRecorderParamExtractor != null) {
            PerfRecorderParameters perfRecorderParameters = perfRecorderParamExtractor.extractFrom(perfAnnotation);
            return instantiatePerfRecorderFrom(perfRecorderClass, perfRecorderParameters);
        } else {
            return instantiatePerfRecorderFrom(perfRecorderClass);
        }
    }

    private static RecordablePerformance instantiatePerfRecorderFrom(Class<? extends RecordablePerformance> perfRecorderClass
                                                                   , PerfRecorderParameters perfRecorderParameters) {
        try {
            Constructor<? extends RecordablePerformance> declaredConstructor = perfRecorderClass.getDeclaredConstructor(PerfRecorderParameters.class);
            return declaredConstructor.newInstance(perfRecorderParameters);
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return RecordablePerformance.NONE;
        }
    }

    private static RecordablePerformance instantiatePerfRecorderFrom(Class<? extends RecordablePerformance> perfRecorderClass) {
        try {
            Constructor<? extends RecordablePerformance> declaredConstructor = perfRecorderClass.getDeclaredConstructor();
            return declaredConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return RecordablePerformance.NONE;
        }
    }

    private static boolean haveQuickPerfAnnotationsToBeDisplayed(Annotation[] perfAnnotations) {
        for (Annotation perfAnnotation : perfAnnotations) {
            if (perfAnnotation.annotationType().equals(DisplayAppliedAnnotations.class)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isQuickPerfDebugMode(Annotation[] perfAnnotations) {
        for (Annotation perfAnnotation : perfAnnotations) {
            if (perfAnnotation.annotationType().equals(DebugQuickPerf.class)) {
                return true;
            }
        }
        return false;
    }

    private static boolean quickPerfIsDisabled(Annotation[] perfAnnotations) {
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

    public int getRunnerAllocationOffset() {
        return runnerAllocationOffset;
    }

    public void cleanResources() {
        for (RecordablePerformance perfRecorder : perfRecordersToExecuteAfterTestMethod) {
            perfRecorder.cleanResources();
        }
    }

}

