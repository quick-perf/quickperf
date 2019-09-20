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

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import org.junit.platform.launcher.core.LauncherFactory;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class QuickPerfJunit5Core {

    public static void main(String... args) {

        String className = args[0];
        String methodName = args[1];
        String workingFolderPath = args[2];

        TestExecutionSummary testResult = runTestMethod(className, methodName);

        List<TestExecutionSummary.Failure> failures = testResult.getFailures();

        JUnit5FailuresRepository jUnit5FailuresRepository = JUnit5FailuresRepository.INSTANCE;

        jUnit5FailuresRepository.save(workingFolderPath, failures);

        // To be sure that tests using Tomcat
        // or Jetty web server will stop
        System.exit(0);

    }

    private static TestExecutionSummary runTestMethod(String className, String methodName) {

        Class<?> aClass = retrieveClassFrom(className);

        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectMethod(aClass, methodName))
                .build();
        Launcher launcher = LauncherFactory.create();
        TestPlan testPlan = launcher.discover(request);//TODO is it necessary ?
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        TestExecutionSummary summary = listener.getSummary();
        summary.printTo(new PrintWriter(System.out));

        return summary;
    }

    private static Class<?> retrieveClassFrom(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " class not found", e);
        }
    }

}
