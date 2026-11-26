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
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class JUnit5Tests {

    private final Launcher launcher;

    private final LauncherDiscoveryRequest request;

    private final SummaryGeneratingListener listener;

    private final TestAbortListener testAbortListener;

    private final ReportEntryListener reportEntryListener;

    private JUnit5Tests(Launcher launcher
                      , LauncherDiscoveryRequest request
                      , SummaryGeneratingListener listener
                      , TestAbortListener testAbortListener
                      , ReportEntryListener reportEntryListener) {
        this.launcher = launcher;
        this.request = request;
        this.listener = listener;
        this.testAbortListener = testAbortListener;
        this.reportEntryListener = reportEntryListener;
    }

    public static JUnit5Tests createInstance(Class<?> testClass) {
        return createInstance(testClass, Collections.<String, String>emptyMap());
    }

    /**
     * @param configurationParameters JUnit configuration parameters, for example to execute the tests concurrently
     */
    public static JUnit5Tests createInstance(Class<?> testClass, Map<String, String> configurationParameters) {
        return createInstance(configurationParameters, testClass);
    }

    /**
     * To execute several test classes in one launcher execution
     *
     * @param configurationParameters JUnit configuration parameters, for example to execute the tests concurrently
     */
    public static JUnit5Tests createInstance(Map<String, String> configurationParameters, Class<?>... testClasses) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        TestAbortListener testAbortListener = new TestAbortListener();
        ReportEntryListener reportEntryListener = new ReportEntryListener();
        List<ClassSelector> classSelectors = new ArrayList<>();
        for (Class<?> testClass : testClasses) {
            classSelectors.add(selectClass(testClass));
        }
        LauncherDiscoveryRequest request =
                LauncherDiscoveryRequestBuilder.request()
                .selectors(classSelectors)
                .configurationParameters(configurationParameters)
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(new SynchronizedListener(listener), testAbortListener, reportEntryListener);
        return new JUnit5Tests(launcher, request, listener, testAbortListener, reportEntryListener);
    }

    public JUnit5TestsResult run()  {
        launcher.execute(request);
        TestExecutionSummary testExecutionSummary = listener.getSummary();
        return new JUnit5TestsResult(testExecutionSummary, testAbortListener.testAborts, reportEntryListener.reportEntries);
    }

    // SummaryGeneratingListener does not collect the failures in a thread-safe way: the tests can be executed concurrently
    private static class SynchronizedListener implements TestExecutionListener {

        private final TestExecutionListener listener;

        private SynchronizedListener(TestExecutionListener listener) {
            this.listener = listener;
        }

        @Override
        public synchronized void testPlanExecutionStarted(TestPlan testPlan) {
            listener.testPlanExecutionStarted(testPlan);
        }

        @Override
        public synchronized void testPlanExecutionFinished(TestPlan testPlan) {
            listener.testPlanExecutionFinished(testPlan);
        }

        @Override
        public synchronized void dynamicTestRegistered(TestIdentifier testIdentifier) {
            listener.dynamicTestRegistered(testIdentifier);
        }

        @Override
        public synchronized void executionSkipped(TestIdentifier testIdentifier, String reason) {
            listener.executionSkipped(testIdentifier, reason);
        }

        @Override
        public synchronized void executionStarted(TestIdentifier testIdentifier) {
            listener.executionStarted(testIdentifier);
        }

        @Override
        public synchronized void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            listener.executionFinished(testIdentifier, testExecutionResult);
        }

        @Override
        public synchronized void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
            listener.reportingEntryPublished(testIdentifier, entry);
        }

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

    private static class ReportEntryListener implements TestExecutionListener {

        private final List<Map<String, String>> reportEntries = new CopyOnWriteArrayList<>();

        @Override
        public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
            reportEntries.add(entry.getKeyValuePairs());
        }

    }

    public static class JUnit5TestsResult {

        private final TestExecutionSummary testExecutionSummary;

        private final List<Throwable> testAborts;

        private final List<Map<String, String>> reportEntries;

        public JUnit5TestsResult(TestExecutionSummary testExecutionSummary, List<Throwable> testAborts) {
            this(testExecutionSummary, testAborts, Collections.<Map<String, String>>emptyList());
        }

        public JUnit5TestsResult(TestExecutionSummary testExecutionSummary
                               , List<Throwable> testAborts
                               , List<Map<String, String>> reportEntries) {
            this.testExecutionSummary = testExecutionSummary;
            this.testAborts = testAborts;
            this.reportEntries = reportEntries;
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

        /**
         * The number of tests whose execution started, to check that the tests of a class failing before them were
         * not executed. A container, such as a test class or a test factory, is not a test.
         */
        public long getNumberOfStartedTests() {
            return testExecutionSummary.getTestsStartedCount();
        }

        /**
         * The key-value pairs of the report entries published during the execution, by the tests and the extensions
         */
        public List<Map<String, String>> getReportEntries() {
            return reportEntries;
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
