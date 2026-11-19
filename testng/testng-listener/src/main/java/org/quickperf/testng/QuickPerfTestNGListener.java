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
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.reporter.QuickPerfReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.Collection;

public class QuickPerfTestNGListener implements IHookable, IInvokedMethodListener {

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    /**
     * Claim the current Surefire pool thread as a QuickPerf test thread BEFORE
     * any TestNG method runs. {@code IInvokedMethodListener.beforeInvocation} is
     * invoked before every method TestNG dispatches, including {@code @BeforeSuite},
     * {@code @BeforeClass}, {@code @BeforeMethod}, {@code @Test}, {@code @AfterMethod},
     * etc., so this naturally covers both class-level and method-level setup SQL
     * (Spring {@code ApplicationContext} startup, Hibernate
     * {@code EntityManagerFactory} creation, schema generation, {@code @Sql}
     * fixture loading, ...).
     * <p>
     * The {@link IHookable#run} path is too late: it only wraps {@code @Test} and
     * runs after {@code @BeforeMethod}. Without this listener, SQL emitted from
     * a user {@code @BeforeMethod} would fall through {@code SqlRecorderRegistry}'s
     * worker fallback and broadcast to every recorder currently live in the
     * process - contaminating sibling tests running concurrently under TestNG
     * {@code parallel=methods} / Surefire {@code parallel=all}.
     * <p>
     * Marking a thread again has no effect, so calling it on every invocation
     * is safe.
     */
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        TestThreads.markCurrentThread();
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        // No-op: kept explicit because some older TestNG variants do not
        // provide a default afterInvocation method on IInvokedMethodListener.
    }

    @Override
    public void run(IHookCallBack hookCallBack, ITestResult testResult) {

        // TestNG executes a @Test(timeOut = ...) method on a thread started for
        // it, after beforeInvocation claimed the thread invoking the method
        TestThreads.markCurrentThread();

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
        if(jvmVersion.isGreaterThanOrEqualTo16()) {
            return 80;
        }
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
            Class<?> testClass = testResult.getInstance().getClass();
            return executeTestMethodInNewJvm(testClass, testMethod, testExecutionContext);
        }

        TestIssue testIssue = executeTestMethodAndRecordPerformanceInSameJvm(hookCallBack, testResult, testExecutionContext);
        return JvmOrTestIssue.buildFrom(testIssue);
    }

    private JvmOrTestIssue executeTestMethodInNewJvm(Class<?> testClass
                                                             , Method testMethod
                                                             , TestExecutionContext testExecutionContext) {
        NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
        return newJvmTestLauncher.executeTestMethodInNewJvm(testClass
                                                          , testMethod
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
