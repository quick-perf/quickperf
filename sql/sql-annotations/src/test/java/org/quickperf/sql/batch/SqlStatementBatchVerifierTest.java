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
package org.quickperf.sql.batch;

import org.junit.Test;
import org.quickperf.issue.PerfIssue;
import org.quickperf.sql.annotation.ExpectJdbcBatching;
import org.quickperf.sql.annotation.SqlAnnotationBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlStatementBatchVerifierTest {

    private final ExpectJdbcBatching expectJdbcBatchingWithBatchSizeOfThree = SqlAnnotationBuilder.expectJdbcBatching(3);

    @Test public void
    should_return_a_perf_issue_if_a_not_batched_execution_follows_a_batch() {

        // GIVEN
        SqlBatchSizes measuredBatchSizes = new SqlBatchSizes(new int[] {3, 0});

        // WHEN
        PerfIssue perfIssue = SqlStatementBatchVerifier.INSTANCE.verifyPerfIssue(expectJdbcBatchingWithBatchSizeOfThree, measuredBatchSizes);

        // THEN
        assertThat(perfIssue.getDescription()).isEqualTo("Expected batch size <3> but is <0>.");

    }

    @Test public void
    should_return_no_perf_issue_if_the_last_batch_is_smaller_than_the_expected_batch_size() {

        // GIVEN
        SqlBatchSizes measuredBatchSizes = new SqlBatchSizes(new int[] {3, 2});

        // WHEN
        PerfIssue perfIssue = SqlStatementBatchVerifier.INSTANCE.verifyPerfIssue(expectJdbcBatchingWithBatchSizeOfThree, measuredBatchSizes);

        // THEN
        assertThat(perfIssue).isEqualTo(PerfIssue.NONE);

    }

    @Test public void
    should_return_no_perf_issue_without_insert_update_and_delete_executions() {

        // GIVEN
        SqlBatchSizes measuredBatchSizes = new SqlBatchSizes(new int[0]);

        // WHEN
        PerfIssue perfIssue = SqlStatementBatchVerifier.INSTANCE.verifyPerfIssue(expectJdbcBatchingWithBatchSizeOfThree, measuredBatchSizes);

        // THEN
        assertThat(perfIssue).isEqualTo(PerfIssue.NONE);

    }

    @Test public void
    should_return_no_perf_issue_if_batching_is_expected_to_be_disabled_and_executions_are_not_batched() {

        // GIVEN
        ExpectJdbcBatching expectJdbcBatchingWithZeroBatchSize = SqlAnnotationBuilder.expectJdbcBatching(0);
        SqlBatchSizes measuredBatchSizes = new SqlBatchSizes(new int[] {0});

        // WHEN
        PerfIssue perfIssue = SqlStatementBatchVerifier.INSTANCE.verifyPerfIssue(expectJdbcBatchingWithZeroBatchSize, measuredBatchSizes);

        // THEN
        assertThat(perfIssue).isEqualTo(PerfIssue.NONE);

    }

}
