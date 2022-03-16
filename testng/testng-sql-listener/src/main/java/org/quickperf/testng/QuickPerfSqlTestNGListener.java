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
package org.quickperf.testng;

import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.issue.TestIssue;
import org.quickperf.perfrecording.PerformanceRecording;
import org.quickperf.reporter.QuickPerfReporter;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Collection;

public class QuickPerfSqlTestNGListener implements IInvokedMethodListener {

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    private TestExecutionContext testExecutionContext;

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        TestExecutionContext testExecutionContext = buildTestExecutionContextFrom(method);
        this.testExecutionContext = testExecutionContext;
        if (!this.testExecutionContext.isQuickPerfDisabled()) {
            performanceRecording.start(this.testExecutionContext);
        }
    }

    private TestExecutionContext buildTestExecutionContextFrom(IInvokedMethod method) {
        ITestNGMethod testNGMethod = method.getTestMethod();
        Method testMethod = testNGMethod.getConstructorOrMethod().getMethod();
        int noAllocationOffsetBecauseOnlyOneJvm = 0;
        return TestExecutionContext.buildFrom(quickPerfConfigs, testMethod, noAllocationOffsetBecauseOnlyOneJvm);
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (this.testExecutionContext.isQuickPerfDisabled()) {
            return;
        }
        quickPerfProcessingAfterMethodExecution(testResult);
    }

    private void quickPerfProcessingAfterMethodExecution(ITestResult testResult) {

        performanceRecording.stop(testExecutionContext);

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();

        JvmOrTestIssue jvmOrTestIssue = buildTestIssueFrom(testResult);

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat =
                perfIssuesEvaluator.evaluatePerfIssuesIfNoJvmIssue( testAnnotationConfigs
                                                                  , testExecutionContext
                                                                  , jvmOrTestIssue);

        testExecutionContext.cleanResources();

        reportIssues(testResult, jvmOrTestIssue, groupOfPerfIssuesToFormat);

    }

    private JvmOrTestIssue buildTestIssueFrom(ITestResult testResult) {
        Throwable throwable = testResult.getThrowable();
        TestIssue testIssue = TestIssue.buildFrom(throwable);
        return JvmOrTestIssue.buildFrom(testIssue);
    }

    private void reportIssues(ITestResult testResult, JvmOrTestIssue jvmOrTestIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        try {
            quickPerfReporter.report(jvmOrTestIssue, groupOfPerfIssuesToFormat, testExecutionContext);
        } catch (Throwable throwable) {
            testResult.setThrowable(throwable);
            testResult.setStatus(ITestResult.FAILURE);
        }
    }

}


