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
import org.quickperf.WorkingFolder;
import org.quickperf.issue.PerfIssue;
import org.quickperf.sql.repository.SqlRepository;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that {@link PersistenceSqlRecorder#findRecord(TestExecutionContext)}
 * returns a measure failing with
 * {@link SqlExecutions#CROSS_TEST_CONTAMINATION_MESSAGE} when the recorder
 * recorded SQL attributed to several recording tests, and only then.
 */
public class PersistenceSqlRecorderCrossTestContaminationTest {

    @After
    public void resetAttributionOfCurrentThreadSql() {
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(false);
    }

    @Test public void
    findRecord_should_return_a_failing_measure_when_sql_was_attributed_to_several_tests() {

        // GIVEN
        SqlExecutions recordedSql = new SqlExecutions();
        PersistenceSqlRecorder recorder = recorderWithRepositoryReturning(recordedSql);
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(new ExecutionInfo(), anInsert());

        // WHEN
        SqlExecutions result = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(result.hasCrossTestContamination()).isTrue();
        assertThat(result.verifyRecord().getDescription())
                .isEqualTo(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);

    }

    @Test public void
    findRecord_should_return_a_measure_without_perf_issue_when_sql_was_attributed_to_a_single_test() {

        // GIVEN
        PersistenceSqlRecorder recorder = recorderWithRepositoryReturning(new SqlExecutions());
        recorder.addQueryExecution(new ExecutionInfo(), anInsert());

        // WHEN
        SqlExecutions result = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(result.hasCrossTestContamination()).isFalse();
        assertThat(result.verifyRecord()).isSameAs(PerfIssue.NONE);

    }

    @Test public void
    findRecord_should_not_return_the_sql_recorded_after_the_first_call() {

        // GIVEN a test that stopped recording, and whose record was found
        SqlExecutions recordedSql = new SqlExecutions();
        PersistenceSqlRecorder recorder = recorderWithRepositoryReturning(recordedSql);
        SqlExecutions firstRecord = recorder.findRecord(mock(TestExecutionContext.class));

        // WHEN a worker thread that found the recorder before the test
        // stopped recording adds SQL attributed to several tests
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(new ExecutionInfo(), anInsert());
        recordedSql.add(new ExecutionInfo(), anInsert());
        SqlExecutions secondRecord = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN the SQL properties of the test are verified with the SQL it
        // recorded
        assertThat(secondRecord).isSameAs(firstRecord);
        assertThat(secondRecord.getNumberOfExecutions()).isZero();
        assertThat(secondRecord.isEmpty()).isTrue();
        assertThat(secondRecord.hasCrossTestContamination()).isFalse();

    }

    @Test public void
    findRecord_should_not_tag_NONE() {

        // GIVEN a recorder whose repository returns the JVM-wide-shared NONE
        // singleton, which must stay untagged
        PersistenceSqlRecorder recorder = recorderWithRepositoryReturning(SqlExecutions.NONE);
        SqlRecorderRegistry.setSqlOfCurrentThreadAttributedToSeveralTests(true);
        recorder.addQueryExecution(new ExecutionInfo(), anInsert());

        // WHEN
        SqlExecutions result = recorder.findRecord(mock(TestExecutionContext.class));

        // THEN
        assertThat(result).isNotSameAs(SqlExecutions.NONE);
        assertThat(result.hasCrossTestContamination()).isTrue();
        assertThat(result.verifyRecord()).isNotSameAs(PerfIssue.NONE);
        assertThat(SqlExecutions.NONE.hasCrossTestContamination()).isFalse();
        assertThat(SqlExecutions.NONE.verifyRecord()).isSameAs(PerfIssue.NONE);

    }

    @Test public void
    contamination_message_should_be_formatted_once_as_a_perf_issue() {

        // GIVEN a contaminated measure
        SqlExecutions contaminated = new SqlExecutions();
        contaminated.markCrossTestContamination();

        // WHEN the measure formats its own perf issue
        String formatted = contaminated.format(Collections.singletonList(contaminated.verifyRecord()));

        // THEN the contamination message appears once, as a [PERF] entry
        String perfEntry = "[PERF] " + SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE;
        assertThat(formatted).contains(perfEntry);
        assertThat(formatted.indexOf(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE))
                .isEqualTo(formatted.lastIndexOf(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE));

        // ... and is not added by format() when the perf issue is not reported
        assertThat(contaminated.format(Collections.<PerfIssue>emptyList()))
                .doesNotContain(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
    }

    private static List<QueryInfo> anInsert() {
        return Collections.singletonList(new QueryInfo("INSERT INTO Book(title) VALUES ('title')"));
    }

    /** Builds a recorder whose private sqlRepository field is a stub returning {@code measure}.
     * Avoids the standard {@code startRecording} path so the test does not have to wire up a
     * real {@link org.quickperf.WorkingFolder} or test-execution context. */
    private static PersistenceSqlRecorder recorderWithRepositoryReturning(SqlExecutions measure) {
        PersistenceSqlRecorder recorder = new PersistenceSqlRecorder();
        SqlRepository repository = mock(SqlRepository.class);
        when(repository.findExecutedQueries(nullable(WorkingFolder.class))).thenReturn(measure);
        try {
            Field f = PersistenceSqlRecorder.class.getDeclaredField("sqlRepository");
            f.setAccessible(true);
            f.set(recorder, repository);
        } catch (Exception e) {
            throw new AssertionError("Failed to inject stub SqlRepository", e);
        }
        return recorder;
    }

}
