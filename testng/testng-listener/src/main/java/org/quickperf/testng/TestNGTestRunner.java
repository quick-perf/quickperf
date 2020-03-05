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

package org.quickperf.testng;

import org.quickperf.issue.BusinessOrTechnicalIssue;
import org.quickperf.testlauncher.TestRunnerFromMain;
import org.testng.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class TestNGTestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    static class TestMethodInterceptor implements IMethodInterceptor {

        private final String testMethodNameToRun;

        TestMethodInterceptor(String testMethodNameToRun) {
            this.testMethodNameToRun = testMethodNameToRun;
        }

        @Override
        public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
            Predicate<IMethodInstance> selectTestMethod =
                    method -> { ITestNGMethod testNGMethod = method.getMethod();
                                String currentMethodName = testNGMethod.getMethodName();
                                return currentMethodName.equals(testMethodNameToRun);
                              };
            IMethodInstance testMethod = methods.stream()
                                                .filter(selectTestMethod)
                                                .findFirst()
                                                .get();
            return Collections.singletonList(testMethod);
        }

    }

    @Override
    public BusinessOrTechnicalIssue executeTestMethod(Class<?> testClass, String methodName) {
        TestNG testNG = createTestNGInstance(testClass, methodName);

        TestListenerAdapter testListenerAdapter = new TestListenerAdapter();
        testNG.addListener(testListenerAdapter);

        testNG.run();

        List<ITestResult> failedTests = testListenerAdapter.getFailedTests();

        List<Throwable> failuresAsThrowables = convertToThrowables(failedTests);

        return BusinessOrTechnicalIssue.buildFrom(failuresAsThrowables);
    }

    private static TestNG createTestNGInstance(Class<?> testClass, String methodName) {
        TestNG result = new TestNG();
        result.setUseDefaultListeners(false);
        result.setVerbose(0);

        Class[] testClasses = {testClass};
        result.setTestClasses(testClasses);

        IMethodInterceptor methodInterceptor = new TestMethodInterceptor(methodName);
        result.setMethodInterceptor(methodInterceptor);

        return result;
    }

    private List<Throwable> convertToThrowables(List<ITestResult> failedTests) {
        return failedTests.stream()
                          .map(failedTest ->
                                 { Throwable throwable = failedTest.getThrowable();
                                   return throwable.getCause();
                                 }
                               )
                          .collect(Collectors.toList());
    }

}
