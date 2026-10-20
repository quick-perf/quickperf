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
package org.quickperf.junit5;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Makes tests executed concurrently by JUnit wait for each other. With the parallel execution, JUnit executes the tests
 * in the workers of a {@link ForkJoinPool}. A worker waiting on a {@link CyclicBarrier} can keep the next dynamic tests
 * of its test factory in its queue, with no other worker taking them: they then start after the timeout of the barrier.
 * With JUnit 5.6.2 and six dynamic tests, it happened in up to one run of two with JDK 11 to 25, and never with JDK 8.
 * Waiting through {@link ForkJoinPool#managedBlock} makes the pool start another worker, which executes them.
 */
public class ForkJoinPoolBarrier {

    private ForkJoinPoolBarrier() { }

    public static void await(CyclicBarrier barrier) throws Exception {
        BarrierBlocker barrierBlocker = new BarrierBlocker(barrier);
        ForkJoinPool.managedBlock(barrierBlocker);
        if (barrierBlocker.failure != null) {
            throw barrierBlocker.failure;
        }
    }

    private static class BarrierBlocker implements ForkJoinPool.ManagedBlocker {

        private final CyclicBarrier barrier;

        private boolean released;

        private Exception failure;

        private BarrierBlocker(CyclicBarrier barrier) {
            this.barrier = barrier;
        }

        @Override
        public boolean block() throws InterruptedException {
            try {
                barrier.await(1, TimeUnit.MINUTES);
            } catch (BrokenBarrierException | TimeoutException barrierFailure) {
                failure = barrierFailure;
            }
            released = true;
            return true;
        }

        @Override
        public boolean isReleasable() {
            return released;
        }

    }

}
