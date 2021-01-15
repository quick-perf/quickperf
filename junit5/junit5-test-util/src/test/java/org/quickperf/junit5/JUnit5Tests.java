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

package org.quickperf.junit5;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class JUnit5Tests {

    private final Launcher launcher;

    private final LauncherDiscoveryRequest request;

    private final SummaryGeneratingListener listener;

    private JUnit5Tests(Launcher launcher
                      , LauncherDiscoveryRequest request
                      , SummaryGeneratingListener listener) {
        this.launcher = launcher;
        this.request = request;
        this.listener = listener;
    }

    public static JUnit5Tests createInstance(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request =
                LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        return new JUnit5Tests(launcher, request, listener);
    }

    public JUnit5TestsResult run()  {
        launcher.execute(request);
        TestExecutionSummary testExecutionSummary = listener.getSummary();
        return new JUnit5TestsResult(testExecutionSummary);
    }

    public static class JUnit5TestsResult {

        private final TestExecutionSummary testExecutionSummary;

        public JUnit5TestsResult(TestExecutionSummary testExecutionSummary) {
            this.testExecutionSummary = testExecutionSummary;
        }

        public int getNumberOfFailures() {
            List<TestExecutionSummary.Failure> failures = testExecutionSummary.getFailures();
            return failures.size();
        }

        public String getErrorReport() {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            testExecutionSummary.printFailuresTo(printWriter);
            return stringWriter.getBuffer().toString();
        }

    }

}
