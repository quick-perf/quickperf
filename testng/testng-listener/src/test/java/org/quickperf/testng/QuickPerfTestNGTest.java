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

package org.quickperf.testng;

import org.quickperf.testng.TestNGTests.TestNGTestsResult;
import org.testng.annotations.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfTestNGTest {

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        Class<?> testClass = TestNGMethodFailing.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining("expected [false] but found [true]");

    }

    @Test public void
    a_test_with_failing_business_code_should_fail_when_the_test_method_is_executed_in_a_specific_jvm() {

        // GIVEN
        Class<?> testClass = TestNGMethodFailingInASpecificJvm.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();

        assertThat(errorReport).hasMessageContaining("expected [false] but found [true]");

        assertThat(errorReport.getCause()).isNull();

        String stackTraceAsString = extractStackTraceAsStringOf(errorReport);
        assertThat(stackTraceAsString).doesNotContain("org.quickperf");

    }

    private String extractStackTraceAsStringOf(Throwable errorReport) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        errorReport.printStackTrace(printWriter);
        return stringWriter.toString();
    }

}
