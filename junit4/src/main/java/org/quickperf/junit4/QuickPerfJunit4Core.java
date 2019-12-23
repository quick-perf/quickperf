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

package org.quickperf.junit4;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.quickperf.BusinessOrTechnicalIssue;
import org.quickperf.repository.BusinessOrTechnicalIssueRepository;

import java.util.ArrayList;
import java.util.List;

public class QuickPerfJunit4Core {

    public static void main(String... args) {

        String className = args[0];
        String methodName = args[1];
        String workingFolderPath = args[2];

        Result testResult = runTestMethod(className, methodName);

        List<Failure> jUnit4Failures = testResult.getFailures();

        List<Throwable> jUnit4failuresAsThrowables = convertJUnit4FailuresToThrowables(jUnit4Failures);

        BusinessOrTechnicalIssue businessOrTechnicalIssue = BusinessOrTechnicalIssue.buildFrom(jUnit4failuresAsThrowables);

        BusinessOrTechnicalIssueRepository businessOrTechnicalIssueRepository = BusinessOrTechnicalIssueRepository.INSTANCE;

        businessOrTechnicalIssueRepository.save(businessOrTechnicalIssue, workingFolderPath);

        // To be sure that tests using Tomcat
        // or Jetty web server will stop
        System.exit(0);

    }

    private static Result runTestMethod(String className, String methodName) {

        Class<?> aClass = retrieveClassFrom(className);

        Request junitRequestOfMethod = Request.method(aClass, methodName);

        return new JUnitCore().run(junitRequestOfMethod);

    }

    private static Class<?> retrieveClassFrom(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(className + " class not found", e);
        }
    }

    private static List<Throwable> convertJUnit4FailuresToThrowables(List<Failure> jUnit4Failures) {
        List<Throwable> throwables = new ArrayList<>();
        for (Failure failure : jUnit4Failures) {
            Throwable throwable = failure.getException();
            throwables.add(throwable);
        }
        return throwables;
    }

}
