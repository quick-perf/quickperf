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
package org.quickperf.sql.time;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;
import org.quickperf.sql.annotation.SqlAnnotationBuilder;
import org.quickperf.time.ExecutionTime;

import java.util.concurrent.TimeUnit;

public class SqlQueryMaxExecutionTimeVerifierTest {

    @Test
    public void should_return_a_perf_issue_if_query_execution_time_is_greater_than_expected() {

        // GIVEN
        VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
        ExpectMaxQueryExecutionTime expectedMaxExecutionTime = SqlAnnotationBuilder.expectMaxQueryExecutionTime(1);
        ExecutionTime sqlExecTime = new ExecutionTime(5, TimeUnit.MILLISECONDS);

        // WHEN
        PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);

        // THEN
        Assertions.assertThat(PerfIssue.NONE).isNotEqualTo(perfIssue);
        Assertions.assertThat(PerfIssue.NONE.getDescription()).isNotEqualTo(perfIssue.getDescription());
        Assertions.assertThat(perfIssue.getDescription())
                .contains("Query execution time expected to be less than <1 ms>")
                .contains("At least one query has a greater execution time. The greater query execution time is <5 ms>");

    }

    @Test
    public void should_return_no_perf_issue_if_query_execution_time_is_less_than_expected() {

        // GIVEN
        VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
        ExpectMaxQueryExecutionTime expectedMaxExecutionTime = SqlAnnotationBuilder.expectMaxQueryExecutionTime(5);
        ExecutionTime sqlExecTime = new ExecutionTime(1, TimeUnit.MILLISECONDS);

        // WHEN
        PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);

        // THEN
        Assertions.assertThat(PerfIssue.NONE).isEqualTo(perfIssue);
        Assertions.assertThat(PerfIssue.NONE.getDescription()).isEqualTo(perfIssue.getDescription());

    }

    @Test
    public void should_return_no_perf_issue_if_query_execution_time_is_same_as_expected() {

        // GIVEN
        VerifiablePerformanceIssue<ExpectMaxQueryExecutionTime, ExecutionTime> verifier = SqlQueryMaxExecutionTimeVerifier.INSTANCE;
        ExpectMaxQueryExecutionTime expectedMaxExecutionTime = SqlAnnotationBuilder.expectMaxQueryExecutionTime(1);
        ExecutionTime sqlExecTime = new ExecutionTime(1, TimeUnit.MILLISECONDS);

        // WHEN
        PerfIssue perfIssue = verifier.verifyPerfIssue(expectedMaxExecutionTime, sqlExecTime);

        // THEN
        Assertions.assertThat(PerfIssue.NONE).isEqualTo(perfIssue);
        Assertions.assertThat(PerfIssue.NONE.getDescription()).isEqualTo(perfIssue.getDescription());

    }

}
