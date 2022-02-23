package org.quickperf.testng;

import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
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

        if(!testExecutionContext.isQuickPerfDisabled()) {
            performanceRecording.stop(testExecutionContext);
        }

        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();

        // Test issue should be taken into account
        JvmOrTestIssue jvmOrTestIssue = JvmOrTestIssue.NONE;

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat =
                perfIssuesEvaluator.evaluatePerfIssuesIfNoJvmIssue( testAnnotationConfigs
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

}


