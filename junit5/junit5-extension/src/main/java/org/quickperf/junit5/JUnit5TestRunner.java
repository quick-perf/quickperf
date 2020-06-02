/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.junit5;

import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.issue.TestIssue;
import org.quickperf.testlauncher.TestRunnerFromMain;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

class JUnit5TestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    @Override
    public TestIssue executeTestMethod(Class<?> testClass, String methodName) {
        TestExecutionSummary testResult = runTestMethod(testClass, methodName);

        List<TestExecutionSummary.Failure> failures = testResult.getFailures();

        List<Throwable> jUnit5failuresAsThrowables = convertToThrowables(failures);

        return TestIssue.buildInNewJvmFrom(jUnit5failuresAsThrowables);
    }

    private List<Throwable> convertToThrowables(List<TestExecutionSummary.Failure> failures) {
        return failures.stream()
                       .map(TestExecutionSummary.Failure::getException)
                       .collect(toList());
    }

    private static TestExecutionSummary runTestMethod(Class<?> testClass, String methodName) {
        MethodSelector testMethodSelector = selectMethod(testClass, methodName);
        LauncherDiscoveryRequest request = request().selectors(testMethodSelector)
                                          .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);

        return listener.getSummary();
    }

}
