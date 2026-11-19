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
package org.quickperf.sql.connection;

import org.junit.After;
import org.junit.Test;
import org.quickperf.TestExecutionContext;
import org.quickperf.measure.BooleanMeasure;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConnectionLeakListenerConcurrencyTest {

    private static final int WORKER_THREADS = 8;
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
    concurrent_acquire_and_release_does_not_throw_or_leak() throws Exception {

        // GIVEN
        final ConnectionLeakListener listener = new ConnectionLeakListener();
        TestExecutionContext ctx = mock(TestExecutionContext.class);
        when(ctx.testExecutionUsesTwoJVMs()).thenReturn(false);
        listener.startRecording(ctx);

        // WHEN — each thread alternates acquire/release on its own pool of connections.
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                Connection[] localConnections = new Connection[OPS_PER_THREAD];
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    localConnections[i] = mock(Connection.class);
                    listener.theDatasourceGetsTheConnection(localConnections[i]);
                }
                for (int i = 0; i < OPS_PER_THREAD; i++) {
                    listener.close(localConnections[i]);
                }
            }
        });

        // THEN
        listener.stopRecording(ctx);
        BooleanMeasure leak = listener.findRecord(ctx);
        assertThat(leak.getValue()).as("paired acquire/release should not produce a leak").isFalse();
    }

    @Test public void
    acquire_only_leaves_connection_tracked() throws Exception {

        // GIVEN
        final ConnectionLeakListener listener = new ConnectionLeakListener();
        TestExecutionContext ctx = mock(TestExecutionContext.class);
        when(ctx.testExecutionUsesTwoJVMs()).thenReturn(false);
        listener.startRecording(ctx);

        // WHEN — workers acquire only, never release.
        runConcurrently(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 100; i++) {
                    listener.theDatasourceGetsTheConnection(mock(Connection.class));
                }
            }
        });

        // THEN
        listener.stopRecording(ctx);
        BooleanMeasure leak = listener.findRecord(ctx);
        assertThat(leak.getValue()).as("acquires without closes should produce a leak").isTrue();
    }

    private void runConcurrently(Runnable task) throws Exception {
        pool = Executors.newFixedThreadPool(WORKER_THREADS);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch done = new CountDownLatch(WORKER_THREADS);
        final AtomicReference<Throwable> firstFailure = new AtomicReference<Throwable>();
        for (int t = 0; t < WORKER_THREADS; t++) {
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
