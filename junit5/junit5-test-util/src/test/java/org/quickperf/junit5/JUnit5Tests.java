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
package org.quickperf.junit5;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class JUnit5Tests {

    private final Launcher launcher;

    private final LauncherDiscoveryRequest request;

    private final SummaryGeneratingListener listener;

    private final TestAbortListener testAbortListener;

    private JUnit5Tests(Launcher launcher
                      , LauncherDiscoveryRequest request
                      , SummaryGeneratingListener listener
                      , TestAbortListener testAbortListener) {
        this.launcher = launcher;
        this.request = request;
        this.listener = listener;
        this.testAbortListener = testAbortListener;
    }

    public static JUnit5Tests createInstance(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        TestAbortListener testAbortListener = new TestAbortListener();
        LauncherDiscoveryRequest request =
                LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener, testAbortListener);
        return new JUnit5Tests(launcher, request, listener, testAbortListener);
    }

    public JUnit5TestsResult run()  {
        launcher.execute(request);
        TestExecutionSummary testExecutionSummary = listener.getSummary();
        return new JUnit5TestsResult(testExecutionSummary, testAbortListener.testAborts);
    }

    private static class TestAbortListener implements TestExecutionListener {

        private final List<Throwable> testAborts = new CopyOnWriteArrayList<>();

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (testIdentifier.isTest() && testExecutionResult.getStatus() == ABORTED
             && testExecutionResult.getThrowable().isPresent()) {
                testAborts.add(testExecutionResult.getThrowable().get());
            }
        }

    }

    public static class JUnit5TestsResult {

        private final TestExecutionSummary testExecutionSummary;

        private final List<Throwable> testAborts;

        public JUnit5TestsResult(TestExecutionSummary testExecutionSummary, List<Throwable> testAborts) {
            this.testExecutionSummary = testExecutionSummary;
            this.testAborts = testAborts;
        }

        public int getNumberOfFailures() {
            List<TestExecutionSummary.Failure> failures = testExecutionSummary.getFailures();
            return failures.size();
        }

        public long getNumberOfAbortedTests() {
            return testExecutionSummary.getTestsAbortedCount();
        }

        public long getNumberOfSucceededTests() {
            return testExecutionSummary.getTestsSucceededCount();
        }

        public List<JvmExecutionReport> getJvmExecutionReports() {
            List<JvmExecutionReport> jvmExecutionReports = new ArrayList<>();
            for (TestExecutionSummary.Failure failure : testExecutionSummary.getFailures()) {
                JvmExecutionReport jvmExecutionReport =
                        JvmExecutionReport.buildFrom(failure.getTestIdentifier(), failure.getException());
                if (jvmExecutionReport != null) {
                    jvmExecutionReports.add(jvmExecutionReport);
                }
            }
            return jvmExecutionReports;
        }

        public String getErrorReport() {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            testExecutionSummary.printFailuresTo(printWriter);
            return stringWriter.getBuffer().toString();
        }

        public String getAbortReport() {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            for (Throwable testAbort : testAborts) {
                testAbort.printStackTrace(printWriter);
            }
            return stringWriter.getBuffer().toString();
        }

    }

}
