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
package org.quickperf.sql;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.junit.After;
import org.junit.Test;
import org.quickperf.TestExecutionContext;
import org.quickperf.issue.PerfIssue;
import org.quickperf.perfrecording.SelfVerifiablePerfRecord;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link SqlStatementBatchRecorder#findRecord(TestExecutionContext)}
 * returns batch sizes failing once with
 * {@link SqlExecutions#CROSS_TEST_CONTAMINATION_MESSAGE} when an insert,
 * update or delete was recorded while the SQL of the current thread was
 * attributed to several recording tests, and only then.
 */
public class SqlStatementBatchRecorderCrossTestContaminationTest {

    @After
    public void resetAttributionOfCurrentThreadSql() {
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(false);
    }

    @Test public void
    findRecord_should_return_failing_batch_sizes_when_an_insert_was_attributed_to_several_tests() {

        // GIVEN
        SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(batchExecution(), queries("INSERT INTO Book(title) VALUES (?)"));

        // WHEN
        SelfVerifiablePerfRecord batchSizes = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(batchSizes.verifyRecord().getDescription())
                .isEqualTo(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);

    }

    @Test public void
    findRecord_should_return_batch_sizes_without_perf_issue_when_only_a_select_was_attributed_to_several_tests() {

        // GIVEN a select, which does not change the batch sizes
        SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(batchExecution(), queries("SELECT title FROM Book"));

        // WHEN
        SelfVerifiablePerfRecord batchSizes = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(batchSizes.verifyRecord()).isSameAs(PerfIssue.NONE);

    }

    @Test public void
    findRecord_should_return_batch_sizes_without_perf_issue_when_the_insert_was_attributed_to_a_single_test() {

        // GIVEN
        SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        recorder.addQueryExecution(batchExecution(), queries("INSERT INTO Book(title) VALUES (?)"));

        // WHEN
        SelfVerifiablePerfRecord batchSizes = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(batchSizes.verifyRecord()).isSameAs(PerfIssue.NONE);

    }

    @Test public void
    findRecord_should_clear_the_contamination_flag_so_a_subsequent_call_does_not_fail_again() {

        // GIVEN a recorder read twice: findRecord returns new batch sizes on every call
        SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(batchExecution(), queries("INSERT INTO Book(title) VALUES (?)"));

        // WHEN
        recorder.findRecord(mock(TestExecutionContext.class));
        SelfVerifiablePerfRecord secondRead = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(secondRead.verifyRecord()).isSameAs(PerfIssue.NONE);

    }

    private static ExecutionInfo batchExecution() {
        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setBatch(true);
        executionInfo.setBatchSize(30);
        return executionInfo;
    }

    private static List<QueryInfo> queries(String query) {
        QueryInfo queryInfo = mock(QueryInfo.class);
        when(queryInfo.getQuery()).thenReturn(query);
        return Collections.singletonList(queryInfo);
    }

}
