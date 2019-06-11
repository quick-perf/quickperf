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

package org.quickperf.sql.select.max;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxSqlSelectTest {

    @Test public void
    should_fail_if_the_number_of_sql_requests_is_greater_than_the_number_expected_with_annotation_on_method() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxSelect.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("You may think that at most <0> select request was sent to the database")
                      .contains("But in fact <1>...")
                      .contains("select")
                      .contains("book0_.id as id1_0_");

        softAssertions.assertAll();

    }

    @Test public void
    should_fail_if_the_number_of_sql_requests_is_greater_than_the_number_expected_with_annotation_on_class() {

        // GIVEN
        Class<?> testClass = AClassAnnotatedWithExpectMaxSelect.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("You may think that at most <0> select request was sent to the database")
                      .contains("But in fact <1>...")
                      .contains("select")
                      .contains("book0_.id as id1_0_");

        softAssertions.assertAll();

    }

    @Test public void
    should_not_fail_if_the_performance_property_is_respected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxSelectAndHavingNoPerfIssue.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @Test public void
    should_fail_if_the_number_of_sql_requests_is_greater_than_the_number_expected_and_test_method_annotated_with_xmx() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxSelectAndWithXmx.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("You may think that at most <0> select request was sent to the database")
                      .contains("But in fact <1>...")
                      .contains("select")
                      .contains("book0_.id as id1_0");

        softAssertions.assertAll();

    }

}
