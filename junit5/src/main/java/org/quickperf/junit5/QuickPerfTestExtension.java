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

package org.quickperf.junit5;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.*;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.reporter.ConsoleReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private final QuickPerfConfigs quickPerfConfigs =  QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();
    private final IssueThrower issueThrower = IssueThrower.INSTANCE;
    private final NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
    private final JUnit5FailuresRepository jUnit5FailuresRepository = JUnit5FailuresRepository.INSTANCE;
    private final ConsoleReporter consoleReporter = ConsoleReporter.INSTANCE;
    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private TestExecutionContext testExecutionContext;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs, extensionContext.getRequiredTestMethod(), JUnitVersion.JUNIT5);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod = testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod();
        List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Object[] args = invocationContext.getArguments().toArray();
            Object target = invocationContext.getTarget().orElse(null);
            Method method = makeAccessible(invocationContext.getExecutable());
            invocation.skip();//skip the invocation as we directly invoke the test method
            if (!testExecutionContext.isQuickPerfDisabled()) {
                startRecording(perfRecordersToExecuteBeforeTestMethod);
            }
            try {
                //directly invoke the method to lower the interaction between JUnit, other extensions and QuickPerf.
                method.invoke(target, args);
            } finally {
                if (!testExecutionContext.isQuickPerfDisabled()) {
                    stopRecording(perfRecordersToExecuteAfterTestMethod);
                }
            }
            return;
        }

        Throwable businessThrowable = null;
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            newJvmTestLauncher.run( invocationContext.getExecutable()
                    , testExecutionContext.getWorkingFolder()
                    , testExecutionContext.getJvmOptions()
                    , QuickPerfJunit5Core.class);

            //skip the invocation as the test method is invoked directly inside the 'newJvmTestLauncher'
            invocation.skip();

            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            businessThrowable = jUnit5FailuresRepository.find(workingFolder);
        }
        else {
            try{
                if(!testExecutionContext.isQuickPerfDisabled()){
                    startRecording(perfRecordersToExecuteBeforeTestMethod);
                }
                invocation.proceed();
            }
            catch (Throwable throwable){
                businessThrowable = throwable;
            }
            finally {
                if(!testExecutionContext.isQuickPerfDisabled()){
                    stopRecording(perfRecordersToExecuteAfterTestMethod);
                }
            }
        }

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat = perfIssuesEvaluator.evaluatePerfIssues(testAnnotationConfigs, testExecutionContext, RetrievableFailure.NONE);

        cleanResources();

        if(testExecutionContext.areQuickPerfAnnotationsToBeDisplayed()) {
            consoleReporter.displayQuickPerfAnnotations(testExecutionContext.getPerfAnnotations());
        }

        if (testExecutionContext.isQuickPerfDebugMode()) {
            consoleReporter.displayQuickPerfDebugInfos();
        }

        issueThrower.throwIfNecessary(businessThrowable, groupOfPerfIssuesToFormat);
    }

    @SuppressWarnings("deprecation")
    private Method makeAccessible(Method executable) {
        if(!executable.isAccessible()){
            executable.setAccessible(true);
        }
        return executable;
    }

    private void startRecording(List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod) {
        for (int i = 0; i < perfRecordersToExecuteBeforeTestMethod.size(); i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteBeforeTestMethod.get(i);
            recordablePerformance.startRecording(testExecutionContext);
        }
    }

    private void stopRecording(List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod) {
        for (int i = 0; i < perfRecordersToExecuteAfterTestMethod.size() ; i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteAfterTestMethod.get(i);
            recordablePerformance.stopRecording(testExecutionContext);
        }
    }

    private void cleanResources() {
        List<RecordablePerformance> perfRecorders = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            perfRecorder.cleanResources();
        }
    }

}