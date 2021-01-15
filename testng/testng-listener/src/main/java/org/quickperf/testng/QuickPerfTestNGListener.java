/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.testng;

import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.issue.TestIssue;
import org.quickperf.jvm.JVM;
import org.quickperf.perfrecording.PerformanceRecording;
import org.quickperf.reporter.QuickPerfReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Collection;

public class QuickPerfTestNGListener implements IHookable {

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    @Override
    public void run(IHookCallBack hookCallBack, ITestResult testResult) {

        TestExecutionContext testExecutionContext = buildTestExecutionContext(testResult);

        if(testExecutionContext.isQuickPerfDisabled()) {
            hookCallBack.runTestMethod(testResult);
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeTestMethodInNewJvmAndRecordPerformance(testResult, testExecutionContext);
            return;
        }

        JvmOrTestIssue jvmOrTestIssue =
                executeTestMethodAndRecordPerformance(hookCallBack, testResult, testExecutionContext);

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat = perfIssuesEvaluator.evaluatePerfIssuesIfNoJvmIssue(testAnnotationConfigs
                                                                                                                    , testExecutionContext
                                                                                                                    , jvmOrTestIssue);

        testExecutionContext.cleanResources();

        try {
            quickPerfReporter.report(jvmOrTestIssue, groupOfPerfIssuesToFormat, testExecutionContext);
        } catch (Throwable throwable) {
            testResult.setThrowable(throwable);
            testResult.setStatus(ITestResult.FAILURE);
        }

    }

    private TestExecutionContext buildTestExecutionContext(ITestResult testResult) {
        Method testMethod = extractTestMethod(testResult);
        int testNGAllocationOffset = findTestNGAllocationOffset();
        return TestExecutionContext.buildFrom(quickPerfConfigs, testMethod, testNGAllocationOffset);
    }

    private int findTestNGAllocationOffset() {
        JVM.Version jvmVersion = JVM.INSTANCE.version;
        if (jvmVersion.isGreaterThanOrEqualTo12()) {
            return 72;
        }
        return 40;
    }

    private Method extractTestMethod(ITestResult testResult) {
        ITestNGMethod testNGMethod = testResult.getMethod();
        return testNGMethod.getConstructorOrMethod().getMethod();
    }

    private void executeTestMethodInNewJvmAndRecordPerformance(ITestResult testResult, TestExecutionContext testExecutionContext) {

        Object[] args = new Object[0];
        Method method = extractTestMethod(testResult);

        performanceRecording.start(testExecutionContext);

        try {
            Object target = testResult.getInstance();
            //directly invoke the method to lower the interaction between JUnit, other extensions and QuickPerf.
            method.invoke(target, args);
        } catch (Throwable throwable) {
            testResult.setThrowable(throwable);
            testResult.setStatus(ITestResult.FAILURE);
        } finally {
            performanceRecording.stop(testExecutionContext);
        }

    }

    private JvmOrTestIssue executeTestMethodAndRecordPerformance(IHookCallBack hookCallBack
                                                                         , ITestResult testResult
                                                                         , TestExecutionContext testExecutionContext) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            Method testMethod = extractTestMethod(testResult);
            return executeTestMethodInNewJwm(testMethod, testExecutionContext);
        }

        TestIssue testIssue = executeTestMethodAndRecordPerformanceInSameJvm(hookCallBack, testResult, testExecutionContext);
        return JvmOrTestIssue.buildFrom(testIssue);
    }

    private JvmOrTestIssue executeTestMethodInNewJwm(Method testMethod
                                                             , TestExecutionContext testExecutionContext) {
        NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
        return newJvmTestLauncher.executeTestMethodInNewJwm(testMethod
                                                          , testExecutionContext
                                                          , QuickPerfTestNGCore.class);
    }

    private TestIssue executeTestMethodAndRecordPerformanceInSameJvm(IHookCallBack hookCallBack, ITestResult testResult, TestExecutionContext testExecutionContext) {
        performanceRecording.start(testExecutionContext);
        try {
            hookCallBack.runTestMethod(testResult);
            return TestIssue.NONE;
        } catch (Throwable throwable) {
            return TestIssue.buildFrom(throwable);
        } finally {
            performanceRecording.stop(testExecutionContext);
        }
    }

}
