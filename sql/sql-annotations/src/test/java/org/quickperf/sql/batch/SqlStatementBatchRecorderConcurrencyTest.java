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

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.StatementType;
import org.junit.After;
import org.junit.Test;
import org.quickperf.TestExecutionContext;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqlStatementBatchRecorderConcurrencyTest {

    private static final int WRITER_THREADS = 8;
    private static final int OPS_PER_THREAD = 5_000;
    private static final int AWAIT_SECONDS  = 30;

    private ExecutorService pool;

    @After
    public void tearDown() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    @Test public void
    concurrent_addQueryExecution_does_not_throw() throws Exception {

        // GIVEN
        final SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        final QueryInfo insertQuery = mock(QueryInfo.class);
        when(insertQuery.getQuery()).thenReturn("INSERT INTO Book(title) VALUES (?)");
        final List<QueryInfo> queries = Collections.singletonList(insertQuery);

        // WHEN
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    ExecutionInfo execInfo = new ExecutionInfo();
                    execInfo.setStatementType(StatementType.PREPARED);
                    execInfo.setBatch(true);
                    execInfo.setBatchSize((i % 5) + 1);
                    recorder.addQueryExecution(execInfo, queries);
                }
            }
        });

        // THEN
        TestExecutionContext ctx = mock(TestExecutionContext.class);
        when(ctx.testExecutionUsesTwoJVMs()).thenReturn(false);
        SqlBatchSizes batchSizes = recorder.findRecord(ctx);
        assertThat(batchSizes.getValue()).hasSize(5)
                                         .doesNotHaveDuplicates();
    }

    @Test public void
    concurrent_addQueryExecution_does_not_lose_distinct_batch_sizes() throws Exception {

        // GIVEN
        final SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
        final QueryInfo insertQuery = mock(QueryInfo.class);
        when(insertQuery.getQuery()).thenReturn("INSERT INTO Book(title) VALUES (?)");
        final List<QueryInfo> queries = Collections.singletonList(insertQuery);
        final int[] expectedSizes = {1, 2, 3, 4, 5, 6, 7, 8};

        // WHEN
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    ExecutionInfo execInfo = new ExecutionInfo();
                    execInfo.setStatementType(StatementType.PREPARED);
                    execInfo.setBatch(true);
                    execInfo.setBatchSize(expectedSizes[i % expectedSizes.length]);
                    recorder.addQueryExecution(execInfo, queries);
                }
            }
        });

        // THEN
        TestExecutionContext ctx = mock(TestExecutionContext.class);
        when(ctx.testExecutionUsesTwoJVMs()).thenReturn(false);
        SqlBatchSizes batchSizes = recorder.findRecord(ctx);
        assertThat(batchSizes.getValue()).hasSize(expectedSizes.length)
                                         .doesNotHaveDuplicates()
                                         .containsOnly(expectedSizes);
    }

    private void runConcurrently(Runnable task) throws Exception {
        pool = Executors.newFixedThreadPool(WRITER_THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(WRITER_THREADS);
        final AtomicReference<Throwable> firstFailure = new AtomicReference<Throwable>();
        for (int t = 0; t < WRITER_THREADS; t++) {
            pool.submit(new WorkerWrapper(task, start, done, firstFailure));
        }
        start.countDown();
        boolean finished = done.await(AWAIT_SECONDS, TimeUnit.SECONDS);
        assertThat(finished).as("workers timed out").isTrue();
        Throwable failure = firstFailure.get();
        if (failure != null) {
            throw new AssertionError("worker thread threw", failure);
        }
    }

    private static final class WorkerWrapper implements Runnable {
        private final Runnable delegate;
        private final CountDownLatch start;
        private final CountDownLatch done;
        private final AtomicReference<Throwable> firstFailure;

        WorkerWrapper(Runnable delegate,
                      CountDownLatch start,
                      CountDownLatch done,
                      AtomicReference<Throwable> firstFailure) {
            this.delegate = delegate;
            this.start = start;
            this.done = done;
            this.firstFailure = firstFailure;
        }

        @Override public void run() {
            try {
                start.await();
                delegate.run();
            } catch (Throwable t) {
                firstFailure.compareAndSet(null, t);
            } finally {
                done.countDown();
            }
        }
    }
}
