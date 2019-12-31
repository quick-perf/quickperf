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

package org.quickperf.testng;

import org.testng.ITestResult;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;

import java.util.List;

public class TestNGTests {

    private final TestNG testNG;

    private final TestListenerAdapter testListenerAdapter;

    private TestNGTests(TestNG testNG, TestListenerAdapter testListenerAdapter) {
        this.testNG = testNG;
        this.testListenerAdapter = testListenerAdapter;
    }

    public static TestNGTests createInstance(Class<?> testClass) {
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        testNG.setVerbose(0);
        Class[] testClasses = {testClass};
        testNG.setTestClasses(testClasses);

        TestListenerAdapter testListenerAdapter = new TestListenerAdapter();
        testNG.addListener(testListenerAdapter);

        return new TestNGTests(testNG, testListenerAdapter);
    }

    public TestNGTestsResult run() {
        testNG.run();
        return new TestNGTestsResult(testListenerAdapter);
    }

    public static class TestNGTestsResult {

        private final TestListenerAdapter testListenerAdapter;

        private TestNGTestsResult(TestListenerAdapter testListenerAdapter) {
            this.testListenerAdapter = testListenerAdapter;
        }

        public int getNumberOfFailedTest() {
            List<ITestResult> failedTests = testListenerAdapter.getFailedTests();
            return failedTests.size();
        }

        public int getNumberOfPassedTest() {
            List<ITestResult> passedTests = testListenerAdapter.getPassedTests();
            return passedTests.size();
        }

        public Throwable getThrowableOfFirstTest() {
            List<ITestResult> failedTests = testListenerAdapter.getFailedTests();
            ITestResult firstFailedTestResult = failedTests.get(0);
            return firstFailedTestResult.getThrowable();
        }

    }

}
