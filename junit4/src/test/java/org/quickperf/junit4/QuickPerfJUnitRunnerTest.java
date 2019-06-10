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

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

import static org.junit.experimental.results.PrintableResult.testResult;

public class QuickPerfJUnitRunnerTest {

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        Class<?> testClass = ClassWithAMethodHavingFailingBusinessCode.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(1);

        softAssertions.assertThat(printableResult.toString()).contains("expected:<[tru]e> but was:<[fals]e>");

        softAssertions.assertAll();

    }

    @Test public void
    a_test_with_failing_business_code_and_a_performance_issue_in_a_specific_jvm() {

        // GIVEN
        Class<?> testClass = ClassWithFuntionalIssueInNewJvmAndPerfIssue.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(1);
        softAssertions.assertThat(printableResult.toString())
                      .contains("Performance and functional properties not respected");
        softAssertions.assertAll();

    }

}
