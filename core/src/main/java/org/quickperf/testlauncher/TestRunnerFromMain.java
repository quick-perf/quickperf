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

package org.quickperf.testlauncher;

import org.quickperf.issue.BusinessOrTechnicalIssue;
import org.quickperf.repository.BusinessOrTechnicalIssueRepository;

public class TestRunnerFromMain {

    public static final TestRunnerFromMain INSTANCE = new TestRunnerFromMain();

    private TestRunnerFromMain() { }

    public interface FrameworkTestRunner {

        BusinessOrTechnicalIssue executeTestMethod(Class<?> testClass, String methodName);

    }

    public void executeTestMethod(FrameworkTestRunner frameworkTestRunner, String... mainArgs) throws ClassNotFoundException {

        MainClassArguments mainClassArguments = MainClassArguments.buildFromMainArguments(mainArgs);

        BusinessOrTechnicalIssue businessOrTechnicalIssue = executeTestMethod(frameworkTestRunner, mainClassArguments);

        BusinessOrTechnicalIssueRepository businessOrTechnicalIssueRepository = BusinessOrTechnicalIssueRepository.INSTANCE;

        String workingFolderPath = mainClassArguments.getWorkingFolderPath();
        businessOrTechnicalIssueRepository.save(businessOrTechnicalIssue
                                              , workingFolderPath);

        // To be sure that Tomcat or Jetty web server will stop
        System.exit(0);

    }

    private BusinessOrTechnicalIssue executeTestMethod(FrameworkTestRunner frameworkTestRunner, MainClassArguments mainClassArguments) throws ClassNotFoundException {
        String className = mainClassArguments.getClassName();
        Class<?> testClass = Class.forName(className);

        String methodName = mainClassArguments.getMethodName();

        return frameworkTestRunner.executeTestMethod(testClass, methodName);
    }

}
