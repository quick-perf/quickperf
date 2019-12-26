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
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.launcher.listeners.TestExecutionSummary.Failure;
import org.quickperf.BusinessOrTechnicalIssue;
import org.quickperf.repository.BusinessOrTechnicalIssueRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

public class QuickPerfJunit5Core {

    public static void main(String... args) {
        String className = args[0];
        String methodName = args[1];
        String workingFolderPath = args[2];

        TestExecutionSummary testResult = runTestMethod(className, methodName);

        List<Failure> failures = testResult.getFailures();

        List<Throwable> jUnit5failuresAsThrowables =  failures.stream()
                                                     .map(Failure::getException)
                                                     .collect(toList());

        BusinessOrTechnicalIssue businessOrTechnicalIssue = BusinessOrTechnicalIssue.buildFrom(jUnit5failuresAsThrowables);

        BusinessOrTechnicalIssueRepository businessOrTechnicalIssueRepository = BusinessOrTechnicalIssueRepository.INSTANCE;

        businessOrTechnicalIssueRepository.save(businessOrTechnicalIssue, workingFolderPath);

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
        launcher.execute(request, listener);

        return listener.getSummary();
    }

    private static Class<?> retrieveClassFrom(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " class not found", e);
        }
    }

}
