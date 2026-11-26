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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.ForkJoinPoolBarrier;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class QuickPerfParallelExecutionTest {

    @QuickPerfUnderTest
    static class ParallelSqlTests extends SqlTestBaseJUnit5 {

        @BeforeEach
        void widenRaceWindow() throws InterruptedException {
            // Widen the window between extension.beforeEach()
            // (writes testExecutionContext) and extension.interceptTestMethod()
            // (reads testExecutionContext).
            Thread.sleep(100);
        }

        // Methods that execute 1 SELECT

        @Test @ExpectSelect(1) void select_a() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_b() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_c() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_d() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_e() { executeOneSelect(); }

        // Methods that execute 0 SELECT

        @Test @ExpectSelect(0) void no_select_a() { }
        @Test @ExpectSelect(0) void no_select_b() { }
        @Test @ExpectSelect(0) void no_select_c() { }
        @Test @ExpectSelect(0) void no_select_d() { }
        @Test @ExpectSelect(0) void no_select_e() { }

        private void executeOneSelect() {
            EntityManager em = emf.createEntityManager();
            em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
        }
    }

    @Test
    void parallel_execution_should_not_corrupt_sql_test_contexts() {

        // GIVEN
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(ParallelSqlTests.class))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "10")
                .build();

        // WHEN
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);
        TestExecutionSummary summary = listener.getSummary();

        // THEN
        assertThat(summary.getTestsSucceededCount()).isEqualTo(10);
        assertThat(summary.getTestsFailedCount()).isZero();
    }

    @QuickPerfUnderTest
    static class ParallelSqlDynamicTests extends SqlTestBaseJUnit5 {

        private static final int NUMBER_OF_DYNAMIC_TESTS = 5;

        // The dynamic tests execute their SELECT once QuickPerf has started recording the SQL executions of all of them
        private static final CyclicBarrier ALL_DYNAMIC_TESTS_RECORDING = new CyclicBarrier(NUMBER_OF_DYNAMIC_TESTS);

        @TestFactory @ExpectSelect(1) Stream<DynamicTest> dynamic_tests_executing_one_select() {
            return IntStream.rangeClosed(1, NUMBER_OF_DYNAMIC_TESTS)
                            .mapToObj(number -> dynamicTest("select " + number, () -> {
                                ForkJoinPoolBarrier.await(ALL_DYNAMIC_TESTS_RECORDING);
                                EntityManager em = emf.createEntityManager();
                                em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
                            }));
        }
    }

    @Test
    void parallel_execution_should_not_corrupt_sql_test_contexts_of_dynamic_tests() {

        // GIVEN
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(ParallelSqlDynamicTests.class))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "10")
                .build();

        // WHEN
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);
        TestExecutionSummary summary = listener.getSummary();

        // THEN
        assertThat(summary.getTestsSucceededCount()).isEqualTo(5);
        assertThat(summary.getTestsFailedCount()).isZero();
    }

    @QuickPerfUnderTest
    static class ParallelSqlDynamicTestsNotRespectingExpectSelect extends SqlTestBaseJUnit5 {

        private static final int NUMBER_OF_DYNAMIC_TESTS = 5;

        private static final CyclicBarrier ALL_DYNAMIC_TESTS_RECORDING = new CyclicBarrier(NUMBER_OF_DYNAMIC_TESTS);

        @TestFactory @ExpectSelect(2) Stream<DynamicTest> dynamic_tests_executing_one_select() {
            return IntStream.rangeClosed(1, NUMBER_OF_DYNAMIC_TESTS)
                            .mapToObj(number -> dynamicTest("select " + number, () -> {
                                ForkJoinPoolBarrier.await(ALL_DYNAMIC_TESTS_RECORDING);
                                EntityManager em = emf.createEntityManager();
                                em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
                            }));
        }
    }

    // The dynamic tests respecting their annotations also pass when QuickPerf intercepts no dynamic test
    @Test
    void should_check_the_sql_executions_of_dynamic_tests_executed_concurrently() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(ParallelSqlDynamicTestsNotRespectingExpectSelect.class
                                                           , concurrentExecution(10));

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(jUnit5TestsResult.getNumberOfFailures()).as(errorReport).isEqualTo(5);
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                               .contains("But there is in fact <1>...")
                               .doesNotContain("Cross-test SQL contamination");
    }

    @QuickPerfUnderTest
    static class TestFactoryExecutingASelectAfterProvidingItsDynamicTest extends SqlTestBaseJUnit5 {

        @TestFactory @ExpectSelect(1) Stream<DynamicTest> dynamic_test_executing_one_select() {
            Thread testFactoryThread = Thread.currentThread();
            CountDownLatch dynamicTestStarted = new CountDownLatch(1);
            CountDownLatch testFactorySelectExecuted = new CountDownLatch(1);
            DynamicTest dynamicTest = dynamicTest("select", () -> {
                dynamicTestStarted.countDown();
                if (Thread.currentThread() != testFactoryThread) {
                    // The test factory method goes on in its thread
                    await(testFactorySelectExecuted, "The test factory method did not execute its select");
                }
                executeOneSelect();
            });
            // JUnit closes the stream once it has provided the dynamic test. If JUnit executes the dynamic test in the thread
            // of the test factory method, it closes the stream after the dynamic test. Otherwise, the select of the test
            // factory method happens during the dynamic test.
            return Stream.of(dynamicTest).onClose(() -> {
                await(dynamicTestStarted, "No thread started the dynamic test");
                executeOneSelect();
                testFactorySelectExecuted.countDown();
            });
        }

        private void executeOneSelect() {
            EntityManager em = emf.createEntityManager();
            em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
        }

        // A timeout fails the test: the select of the test factory method could otherwise happen outside the dynamic test,
        // and the test pass without checking anything. The thread of the test factory method waits in the ForkJoinPool
        // of JUnit: the pool can start another worker to execute the dynamic test.
        private static void await(CountDownLatch latch, String timeoutMessage) {
            try {
                ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                    @Override
                    public boolean block() throws InterruptedException {
                        if (!latch.await(1, TimeUnit.MINUTES)) {
                            throw new IllegalStateException(timeoutMessage);
                        }
                        return true;
                    }

                    @Override
                    public boolean isReleasable() {
                        return latch.getCount() == 0;
                    }
                });
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(interruptedException);
            }
        }
    }

    // The thread of the test factory method is a test thread: its SQL is not recorded for the dynamic test. In a JVM
    // started by quick-perf-junit5, see QuickPerfParallelExecutionInNewJvmTest of junit5-sql-test.
    @Test
    void should_only_record_the_sql_executions_of_the_dynamic_test_with_the_parallel_execution_enabled() {

        // GIVEN
        Class<?> testClass = TestFactoryExecutingASelectAfterProvidingItsDynamicTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass, concurrentExecution(2));

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();
    }

    // With a parallelism of 2, another thread can execute a dynamic test, even on a single-processor machine
    private static Map<String, String> concurrentExecution(int parallelism) {
        Map<String, String> concurrentExecution = new HashMap<>();
        concurrentExecution.put("junit.jupiter.execution.parallel.enabled", "true");
        concurrentExecution.put("junit.jupiter.execution.parallel.mode.default", "concurrent");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.strategy", "fixed");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.fixed.parallelism", String.valueOf(parallelism));
        return concurrentExecution;
    }

}
