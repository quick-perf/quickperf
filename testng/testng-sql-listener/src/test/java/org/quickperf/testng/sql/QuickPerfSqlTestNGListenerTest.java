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
package org.quickperf.testng.sql;

import org.quickperf.testng.TestNGTests;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfSqlTestNGListenerTest {

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        Class<?> testClass = TestNGMethodFailingWithQuickPerfSqlTestNGListener.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining("expected [false] but found [true]");

    }

    @Test public void
    a_test_should_fail_if_the_number_of_sql_statements_is_not_as_expected() {

        // GIVEN
        Class<?> testClass = SqlSelectWithQuickPerfSqlTestNGListener.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining("a performance-related property is not respected")
                               .hasMessageContaining("You may think that <5> select statements were sent to the database")
                               .hasMessageContaining("But there is in fact <1>...");

    }


    @Test public void
    disable_quick_perf_annotation_should_disable_quick_perf_features() {

        // GIVEN
        Class<?> testClass = QuickPerfSqlTestNGListenerAndDisableQuickPerf.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfPassedTest()).isOne();

    }

    @Test public void
    quickperf_should_report_issues_on_performance_and_functional_properties() {


        // GIVEN
        Class<?> testClass = QuickPerfSqlTestNGListenerAndFailingBusinessBehaviorAndSelectNumberIssue.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport)
                .hasMessageContaining("Performance-related and functional properties not respected");

    }

}
