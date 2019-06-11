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

package org.quickperf.sql.batch;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

public class ExpectJdbcBatchingTest {

    @Test public void
    a_method_annotated_with_jdbc_batches_and_with_batch_size_a_multiple_of_rows_to_insert() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(0);
        softAssertions.assertAll();

    }

    @Test public void
    should_not_fail_if_last_batch_execution_has_a_batch_size_different_from_the_expected_batch_size() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(0);
        softAssertions.assertAll();

    }

    @Test public void
    a_method_annotated_with_jdbc_batches_and_insert_not_batched_in_new_jvm() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsertInNewJvm.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);
        softAssertions.assertAll();
    }

    @Test public void
    a_method_annotated_with_jdbc_batches_in_new_jvm() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(0);
        softAssertions.assertAll();
    }

    @Test public void
    should_fail_with_a_method_annotated_with_jdbc_batches_and_query_not_batched() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("[PERF] Expected batch size <30> but is <0>.");

        softAssertions.assertAll();
    }

    @Test public void
    should_verify_that_sql_orders_are_batched_without_checking_batch_size() {

        // GIVEN
        Class<?> testClass = AClassHavingAPassingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(0);
        softAssertions.assertAll();

    }

    @Test public void
    should_fail_with_not_batched_query_and_no_batch_size_parameter_in_expect_jdbc_batching_annotation() {

        // GIVEN
        Class<?> testClass = AClassHavingAFailingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("[PERF] SQL executions were supposed to be batched.");

        softAssertions.assertAll();

    }

}
