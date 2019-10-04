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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final IssueThrower issueThrower = IssueThrower.INSTANCE;

    private final ConsoleReporter consoleReporter = ConsoleReporter.INSTANCE;

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private TestExecutionContext testExecutionContext;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs, extensionContext.getRequiredTestMethod());
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod = testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod();
        List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();

        startRecordings(perfRecordersToExecuteBeforeTestMethod);
        Throwable businessThrowable = null;
        try{
            invocation.proceed();
        }
        catch (Throwable throwable){
            businessThrowable = throwable;
        }
        finally {
            stopRecordings(perfRecordersToExecuteAfterTestMethod);
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

    private void startRecordings(List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod) {
        for (int i = 0; i < perfRecordersToExecuteBeforeTestMethod.size(); i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteBeforeTestMethod.get(i);
            recordablePerformance.startRecording(testExecutionContext);
        }
    }

    private void stopRecordings(List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod) {
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
