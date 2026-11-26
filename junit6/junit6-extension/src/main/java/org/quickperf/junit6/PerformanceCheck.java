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

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.issue.TestIssue;
import org.quickperf.perfrecording.PerformanceRecording;
import org.quickperf.reporter.QuickPerfReporter;

import java.util.Collection;

/**
 * Executes a test method in the test JVM, and checks its performance.
 */
final class PerformanceCheck {

    private PerformanceCheck() { }

    /**
     * Records the performance of the test method execution, and evaluates the performance-related properties. The
     * caller releases the resources of the context: in afterEach for a test method, so that the recorders registered
     * when the context was built (@DisplaySql, @ProfileConnection) also record the @AfterEach methods, and right after
     * this check for a dynamic test.
     *
     * @throws Throwable what QuickPerfReporter throws: the throwable of the test method, an AssertionError reporting the
     * performance issues, or an AssertionError combining both. A failure to start the recording propagates unchanged.
     */
    static void executeAndCheck(TestExecutionContext testExecutionContext
                              , QuickPerfConfigs quickPerfConfigs
                              , Invocation<Void> testMethodInvocation) throws Throwable {

        // Outside the try block recording the test method: stopping recorders that did not start would hide a start
        // failure behind another exception, or behind a performance issue measured on an empty record
        PerformanceRecording.INSTANCE.start(testExecutionContext);
        TestIssue testIssue = executeAndRecord(testExecutionContext, testMethodInvocation);
        JvmOrTestIssue jvmOrTestIssue = JvmOrTestIssue.buildFrom(testIssue);
        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat =
                PerfIssuesEvaluator.INSTANCE.evaluatePerfIssuesIfNoJvmIssue(quickPerfConfigs.getTestAnnotationConfigs()
                                                                          , testExecutionContext
                                                                          , jvmOrTestIssue);

        QuickPerfReporter.INSTANCE.report(jvmOrTestIssue, groupOfPerfIssuesToFormat, testExecutionContext);

    }

    private static TestIssue executeAndRecord(TestExecutionContext testExecutionContext
                                            , Invocation<Void> testMethodInvocation) {
        try {
            testMethodInvocation.proceed();
            return TestIssue.NONE;
        } catch (Throwable throwable) {
            // An abort thrown by the test method stays an abort: the performance-related properties are not evaluated
            return TestIssue.buildFrom(throwable);
        } finally {
            PerformanceRecording.INSTANCE.stop(testExecutionContext);
        }
    }

}
