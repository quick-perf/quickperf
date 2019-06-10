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

import java.util.List;

public class QuickPerfJunit4Core {

    public static void main(String... args) {

        String className = args[0];
        String methodName = args[1];
        String workingFolderPath = args[2];

        Result testResult = runTestMethod(className, methodName);

        List<Failure> failures = testResult.getFailures();

        JUnit4FailuresRepository jUnit4FailuresRepository = JUnit4FailuresRepository.INSTANCE;

        jUnit4FailuresRepository.save(workingFolderPath, failures);

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

}
