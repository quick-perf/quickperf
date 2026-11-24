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
package org.quickperf.testng.jvm.skip;

import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SkipExceptionInNewJvmTestNGTest {

    @Test public void
    should_report_a_test_skipped_by_its_test_method_in_the_specific_jvm_as_skipped_without_evaluating_its_performance() {

        // GIVEN
        Class<?> testClass = TestNGMethodAllocatingAndThenSkippedInASpecificJvm.class;

        // WHEN
        TestListenerAdapter testsResult = run(testClass);

        // THEN
        String report = buildReportOf(testsResult);

        assertThat(testsResult.getFailedTests())
                .as("The skip cuts the heap allocation measure short, so QuickPerf should not evaluate it.%n%s"
                  , report)
                .isEmpty();

        assertThat(testsResult.getSkippedTests())
                .as("The test skipped in the specific JVM should be skipped.%n%s", report)
                .hasSize(1);

        Throwable skipException = testsResult.getSkippedTests().get(0).getThrowable();
        assertThat(skipException)
                .as(report)
                .isInstanceOf(SkipException.class)
                .hasMessage("Skipped after allocating 3 MB");

        assertThat(report).doesNotContain("heap allocation");

    }

    @Test public void
    should_report_a_test_skipped_by_its_test_method_in_the_specific_jvm_as_skipped() {

        // GIVEN
        Class<?> testClass = TestNGMethodSkippedInASpecificJvm.class;

        // WHEN
        TestListenerAdapter testsResult = run(testClass);

        // THEN
        String report = buildReportOf(testsResult);

        assertThat(testsResult.getFailedTests())
                .as(report)
                .isEmpty();

        assertThat(testsResult.getSkippedTests())
                .as("The test skipped in the specific JVM should be skipped.%n%s", report)
                .hasSize(1);

        Throwable skipException = testsResult.getSkippedTests().get(0).getThrowable();
        assertThat(skipException)
                .as(report)
                .isInstanceOf(SkipException.class)
                .hasMessage("Skipped without allocating");

    }

    // TestNGTests does not give the skipped tests
    private static TestListenerAdapter run(Class<?> testClass) {
        TestNG testNG = new TestNG();
        testNG.setUseDefaultListeners(false);
        testNG.setVerbose(0);
        Class[] testClasses = {testClass};
        testNG.setTestClasses(testClasses);

        TestListenerAdapter testListenerAdapter = new TestListenerAdapter();
        testNG.addListener(testListenerAdapter);

        testNG.run();

        return testListenerAdapter;
    }

    private static String buildReportOf(TestListenerAdapter testsResult) {
        return "Failed tests:" + System.lineSeparator()
             + buildReportOf(testsResult.getFailedTests())
             + "Skipped tests:" + System.lineSeparator()
             + buildReportOf(testsResult.getSkippedTests());
    }

    private static String buildReportOf(List<ITestResult> testResults) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        for (ITestResult testResult : testResults) {
            printWriter.println(testResult.getName());
            Throwable throwable = testResult.getThrowable();
            if (throwable != null) {
                throwable.printStackTrace(printWriter);
            }
        }
        printWriter.flush();
        return stringWriter.toString();
    }

}
