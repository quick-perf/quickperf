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

import org.junit.After;
import org.junit.Test;
import org.quickperf.TestExecutionContext;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;
import org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SqlRecorderRegistryTest {

    @After
    public void tearDown() {
        // Surefire runs this module with parallel=all, threadCount=5; clear
        // any state this test left behind so it cannot leak into siblings.
        SqlRecorderRegistry.INSTANCE.clear();
    }

    @Test public void
    should_get_a_sql_recorder_from_its_type() {

        // GIVEN
        SqlRecorder registeredSqlRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(registeredSqlRecorder);

        // WHEN
        SqlRecorder retrievedSqlRecorder = SqlRecorderRegistry.INSTANCE
                                          .getSqlRecorderOfType(PersistenceSqlRecorder.class);

        // THEN
        assertThat(retrievedSqlRecorder).isEqualTo(registeredSqlRecorder);

    }

    @Test public void
    should_clear_sql_recorder_registry() {

        // GIVEN
        SqlRecorder registeredSqlRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(registeredSqlRecorder);

        // WHEN
        SqlRecorderRegistry.INSTANCE.clear();

        // THEN
        Collection<SqlRecorder> sqlRecorders = SqlRecorderRegistry.INSTANCE.getSqlRecorders();
        assertThat(sqlRecorders).hasSize(0);

    }

    @Test public void
    register_then_get_returns_recorder_on_unrelated_worker_thread() throws Exception {

        // GIVEN a worker thread started before the test registers its
        // recorder, as an HTTP worker or an executor thread: it does not
        // inherit anything from the test thread
        final CountDownLatch recorderRegistered = new CountDownLatch(1);
        final AtomicReference<Collection<SqlRecorder>> workerView
                = new AtomicReference<Collection<SqlRecorder>>();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    recorderRegistered.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                workerView.set(SqlRecorderRegistry.INSTANCE.getSqlRecorders());
            }
        }, "registry-worker-fallback-smoke");
        worker.start();

        // WHEN the test registers its recorder, then the worker reads the
        // registry: it has no PER_THREAD_RECORDERS map, so it falls back to
        // ACTIVE_RECORDERS
        SqlRecorder testThreadRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(testThreadRecorder);
        recorderRegistered.countDown();
        worker.join();

        // THEN
        assertThat(workerView.get()).contains(testThreadRecorder);

    }

    @Test public void
    should_not_give_a_replaced_recorder_to_worker_threads() throws InterruptedException {

        // GIVEN a recorder never unregistered, as after a test that leaked
        // it, then a recorder of the same class registered on the same thread
        SqlRecorder leakedRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(leakedRecorder);
        SqlRecorder replacingRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(replacingRecorder);

        // WHEN
        Collection<SqlRecorder> sqlRecordersOfAWorkerThread = getSqlRecordersOfAWorkerThread();

        // THEN: not an exact check, as the tests run in parallel may register
        // recorders at the same time
        assertThat(sqlRecordersOfAWorkerThread).contains(replacingRecorder)
                                               .doesNotContain(leakedRecorder);

    }

    @Test public void
    should_not_give_the_recorders_of_a_test_whose_resources_are_cleaned_to_worker_threads() throws Exception {

        // GIVEN recorders started by the thread executing a timed-out test,
        // which keeps running and never stops recording
        final TestExecutionContext testExecutionContext = mock(TestExecutionContext.class);
        final List<SqlRecorder> recorders = Arrays.<SqlRecorder>asList( new PersistenceSqlRecorder()
                                                                      , new SqlStatementBatchRecorder()
                                                                      , new DisplaySqlOfTestMethodBodyRecorder());
        ExecutorService timedOutTestThread = Executors.newSingleThreadExecutor();
        try {
            timedOutTestThread.submit(new Runnable() {
                @Override
                public void run() {
                    for (SqlRecorder recorder : recorders) {
                        recorder.startRecording(testExecutionContext);
                    }
                }
            }).get(5, TimeUnit.SECONDS);
        } finally {
            timedOutTestThread.shutdownNow();
        }

        // WHEN the runner thread cleans the resources of the test
        for (SqlRecorder recorder : recorders) {
            recorder.cleanResources();
        }

        // THEN
        assertThat(getSqlRecordersOfAWorkerThread()).doesNotContainAnyElementsOf(recorders);

    }

    @Test public void
    a_recorder_lookup_on_a_worker_thread_should_not_attribute_its_sql_to_several_tests() throws Exception {

        // GIVEN two tests recording SQL
        SqlRecorder recorderOfATest = startRecordingOnANewThread();
        SqlRecorder recorderOfAnotherTest = startRecordingOnANewThread();

        try {

            // WHEN a worker thread looks a recorder up
            final AtomicBoolean sqlOfTheWorkerAttributedToSeveralTests = new AtomicBoolean();
            Thread worker = new Thread(new Runnable() {
                @Override
                public void run() {
                    SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(SqlStatementBatchRecorder.class);
                    sqlOfTheWorkerAttributedToSeveralTests.set(SqlRecorderRegistry.isSqlOfCurrentThreadAttributedToSeveralTests());
                }
            }, "sql-recorder-registry-worker");
            worker.start();
            worker.join();

            // THEN the lookup records no SQL
            assertThat(sqlOfTheWorkerAttributedToSeveralTests.get()).isFalse();

        } finally {
            SqlRecorderRegistry.unregister(recorderOfATest);
            SqlRecorderRegistry.unregister(recorderOfAnotherTest);
        }

    }

    private static SqlRecorder startRecordingOnANewThread() throws Exception {
        ExecutorService testThread = Executors.newSingleThreadExecutor();
        try {
            return testThread.submit(new Callable<SqlRecorder>() {
                @Override
                public SqlRecorder call() {
                    SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
                    recorder.startRecording(null);
                    return recorder;
                }
            }).get(5, TimeUnit.SECONDS);
        } finally {
            testThread.shutdownNow();
        }
    }

    private static Collection<SqlRecorder> getSqlRecordersOfAWorkerThread() throws InterruptedException {
        final AtomicReference<Collection<SqlRecorder>> sqlRecorders = new AtomicReference<Collection<SqlRecorder>>();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                sqlRecorders.set(SqlRecorderRegistry.INSTANCE.getSqlRecorders());
            }
        }, "sql-recorder-registry-worker");
        worker.start();
        worker.join();
        return sqlRecorders.get();
    }

}