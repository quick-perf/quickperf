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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlExecutionsConcurrencyTest {

    // Tunables — chosen low enough to keep the test sub-second under
    // Surefire `parallel=all, threadCount=5`, high enough to reliably
    // surface ArrayDeque corruption on master.
    private static final int WRITER_THREADS  = 8;
    private static final int OPS_PER_THREAD  = 10_000;
    private static final int AWAIT_SECONDS   = 30;

    private ExecutorService pool;

    @After
    public void tearDown() {
        if (pool != null) {
            pool.shutdownNow();
        }
    }

    @Test public void
    sqlExecutions_addLast_under_concurrent_writers_does_not_throw_or_lose_entries() throws Exception {

        // GIVEN
        final SqlExecutions sqlExecutions = new SqlExecutions();
        final ExecutionInfo execInfo = new ExecutionInfo();
        final List<QueryInfo> queries = Collections.emptyList();

        // WHEN
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    sqlExecutions.add(execInfo, queries);
                }
            }
        });

        // THEN
        int iteratorCount = 0;
        for (Iterator<SqlExecution> it = sqlExecutions.iterator(); it.hasNext(); ) {
            it.next();
            iteratorCount++;
        }
        assertThat(iteratorCount).isEqualTo(WRITER_THREADS * OPS_PER_THREAD);
    }

    @Test public void
    sqlExecutions_getNumberOfExecutions_matches_actual_count_under_concurrent_writes() throws Exception {

        // GIVEN
        final SqlExecutions sqlExecutions = new SqlExecutions();
        final ExecutionInfo execInfo = new ExecutionInfo();
        final List<QueryInfo> queries = Collections.emptyList();

        // WHEN
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    sqlExecutions.add(execInfo, queries);
                }
            }
        });

        // THEN
        int iteratorCount = 0;
        for (Iterator<SqlExecution> it = sqlExecutions.iterator(); it.hasNext(); ) {
            it.next();
            iteratorCount++;
        }
        int total = WRITER_THREADS * OPS_PER_THREAD;
        assertThat(sqlExecutions.getNumberOfExecutions()).isEqualTo(total);
        assertThat(sqlExecutions.getNumberOfExecutions()).isEqualTo(iteratorCount);
    }

    @Test public void
    sqlExecutions_getNumberOfExecutions_progresses_safely_during_concurrent_writes() throws Exception {

        // GIVEN
        final SqlExecutions sqlExecutions = new SqlExecutions();
        final ExecutionInfo execInfo = new ExecutionInfo();
        final List<QueryInfo> queries = Collections.emptyList();
        final int writerThreads = WRITER_THREADS - 1;
        final int totalWrites = writerThreads * OPS_PER_THREAD;
        pool = Executors.newFixedThreadPool(WRITER_THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(writerThreads);
        final AtomicReference<Throwable> firstFailure = new AtomicReference<Throwable>();

        for (int t = 0; t < writerThreads; t++) {
            pool.submit(new WorkerWrapper(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < OPS_PER_THREAD; i++) {
                        sqlExecutions.add(execInfo, queries);
                    }
                }
            }, start, done, firstFailure));
        }

        // Reader thread — loops monotonic-checking until writers finish.
        final AtomicReference<Throwable> readerFailure = new AtomicReference<Throwable>();
        final CountDownLatch readerDone = new CountDownLatch(1);
        pool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    start.await();
                    int previous = 0;
                    while (done.getCount() > 0) {
                        int current = sqlExecutions.getNumberOfExecutions();
                        if (current < previous) {
                            throw new AssertionError(
                                    "getNumberOfExecutions decreased: was " + previous + ", now " + current);
                        }
                        previous = current;
                    }
                    // One last read after writers finish.
                    int finalCount = sqlExecutions.getNumberOfExecutions();
                    if (finalCount < previous) {
                        throw new AssertionError(
                                "getNumberOfExecutions decreased after writers finished: was "
                                        + previous + ", now " + finalCount);
                    }
                } catch (Throwable t) {
                    readerFailure.compareAndSet(null, t);
                } finally {
                    readerDone.countDown();
                }
            }
        });

        // WHEN
        start.countDown();
        boolean writersFinished = done.await(AWAIT_SECONDS, TimeUnit.SECONDS);
        boolean readerFinished = readerDone.await(AWAIT_SECONDS, TimeUnit.SECONDS);

        // THEN
        assertThat(writersFinished).as("writers timed out").isTrue();
        assertThat(readerFinished).as("reader timed out").isTrue();
        Throwable wf = firstFailure.get();
        if (wf != null) {
            throw new AssertionError("worker thread threw", wf);
        }
        Throwable rf = readerFailure.get();
        if (rf != null) {
            throw new AssertionError("reader thread threw", rf);
        }
        assertThat(sqlExecutions.getNumberOfExecutions()).isEqualTo(totalWrites);
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

    // Static nested class because JDK 1.7 has no lambdas.
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
