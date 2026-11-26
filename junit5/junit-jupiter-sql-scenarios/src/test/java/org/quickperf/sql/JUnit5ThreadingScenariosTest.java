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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.DisableGlobalAnnotations;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectJdbcBatching;
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder;
import org.quickperf.sql.display.DisplaySqlRecorder;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

class JUnit5ThreadingScenariosTest {

    private static ThreadFactory daemonThreadFactory(final String name) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, name);
                t.setDaemon(true);
                return t;
            }
        };
    }

    private static void prestartWorkerThreads(ExecutorService executorService, int numberOfWorkers) {
        final CountDownLatch workersStarted = new CountDownLatch(numberOfWorkers);
        final CountDownLatch releaseWorkers = new CountDownLatch(1);
        Future<?>[] futures = new Future<?>[numberOfWorkers];
        try {
            for (int i = 0; i < numberOfWorkers; i++) {
                futures[i] = executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        workersStarted.countDown();
                        try {
                            releaseWorkers.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
            if (!workersStarted.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Executor did not create the expected worker threads");
            }
            releaseWorkers.countDown();
            for (Future<?> future : futures) {
                future.get(5, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            releaseWorkers.countDown();
            throw new RuntimeException(e);
        }
    }

    private static void forceSchedulerWorkerCreation(ScheduledExecutorService scheduler) {
        try {
            scheduler.schedule(new Runnable() {
                @Override
                public void run() { }
            }, 0, TimeUnit.MILLISECONDS).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void executeSelect(EntityManagerFactory entityManagerFactory) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
        } finally {
            em.close();
        }
    }

    private static Future<?> submitSelect(ExecutorService executorService,
                                          final EntityManagerFactory entityManagerFactory) {
        return executorService.submit(new Runnable() {
            @Override
            public void run() {
                executeSelect(entityManagerFactory);
            }
        });
    }

    private static void submitSelectAndAwait(ExecutorService executorService,
                                             final EntityManagerFactory entityManagerFactory) throws Exception {
        Future<?> future = submitSelect(executorService, entityManagerFactory);
        future.get(5, TimeUnit.SECONDS);
    }

    private static void scheduleSelectAndAwait(ScheduledExecutorService scheduler,
                                               final EntityManagerFactory entityManagerFactory) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        scheduler.schedule(selectTask(entityManagerFactory, latch), 0, TimeUnit.MILLISECONDS);
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    private static void enqueueSelectAndAwait(BlockingQueue<Runnable> tasks,
                                              final EntityManagerFactory entityManagerFactory) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        tasks.put(selectTask(entityManagerFactory, latch));
        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }

    private static Runnable selectTask(final EntityManagerFactory entityManagerFactory,
                                       final CountDownLatch latch) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    executeSelect(entityManagerFactory);
                } finally {
                    latch.countDown();
                }
            }
        };
    }

    private static JUnit5TestsResult run(Class<?> testClass) {
        return JUnit5Tests.createInstance(testClass).run();
    }

    private static void assertNoFailure(Class<?> testClass, String description) {
        JUnit5TestsResult result = run(testClass);
        assertThat(result.getNumberOfFailures())
                .as(description + ": %s", result.getErrorReport())
                .isZero();
    }

    private static TestExecutionSummary runInParallel(Class<?> testClass, int parallelism,
                                                      TestExecutionListener... additionalListeners) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", String.valueOf(parallelism))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, withListener(listener, additionalListeners));
        return listener.getSummary();
    }

    private static TestExecutionSummary runSequentially(Class<?> testClass,
                                                        TestExecutionListener... additionalListeners) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, withListener(listener, additionalListeners));
        return listener.getSummary();
    }

    private static TestExecutionListener[] withListener(TestExecutionListener listener,
                                                        TestExecutionListener[] otherListeners) {
        TestExecutionListener[] listeners = Arrays.copyOf(otherListeners, otherListeners.length + 1);
        listeners[otherListeners.length] = listener;
        return listeners;
    }

    /** Collects, by test method name, the exception message (and its cause
     * chain) of each failed test so tests can grep for the cross-test
     * contamination error that QuickPerf reports as a perf issue. Unlike the
     * failure list of TestExecutionSummary in JUnit Platform 1.6, it does not
     * lose the failure of one of two tests finishing concurrently. */
    private static class FailureMessages implements TestExecutionListener {

        private final ConcurrentMap<String, String> messageByTestMethodName = new ConcurrentHashMap<>();

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (!testIdentifier.isTest() || testExecutionResult.getStatus() != TestExecutionResult.Status.FAILED) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            Throwable t = testExecutionResult.getThrowable().orElse(null);
            while (t != null) {
                if (t.getMessage() != null) {
                    sb.append(t.getMessage()).append(System.lineSeparator());
                }
                t = t.getCause();
            }
            messageByTestMethodName.put(testMethodNameOf(testIdentifier), sb.toString());
        }

        private static String testMethodNameOf(TestIdentifier testIdentifier) {
            String displayName = testIdentifier.getDisplayName();
            int parametersStart = displayName.indexOf('(');
            return parametersStart < 0 ? displayName : displayName.substring(0, parametersStart);
        }

        String of(String testMethodName) {
            String message = messageByTestMethodName.get(testMethodName);
            return message == null ? "" : message;
        }

    }

    // =========================================================================
    // SQL on a pre-existing Tomcat-style HTTP worker thread
    // =========================================================================
    // A Spring Boot application's Tomcat thread pool is created at startup,
    // before a test method starts. SQL triggered by an HTTP request — even one
    // sent from inside the test method — must still be recorded for the running
    // QuickPerf test.
    //
    // We simulate the Tomcat pool with a static ExecutorService started before
    // the QuickPerf test method runs. The threads are named like Tomcat's NIO
    // connector ("http-nio-8080-exec-N") for readability only — QuickPerf does
    // not key behavior off the thread name.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingTomcatStylePool extends SqlTestBaseJUnit5 {

        private static final ExecutorService TOMCAT_LIKE_POOL;
        static {
            final AtomicInteger counter = new AtomicInteger();
            TOMCAT_LIKE_POOL = Executors.newFixedThreadPool(2, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "http-nio-8080-exec-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });
            prestartWorkerThreads(TOMCAT_LIKE_POOL, 2);
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_pre_existing_tomcat_thread() throws Exception {
            submitSelectAndAwait(TOMCAT_LIKE_POOL, emf);
        }
    }

    @Test
    void sql_from_pre_existing_tomcat_thread_pool_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingTomcatStylePool.class,
                "SQL executed on a pre-existing Tomcat-style HTTP worker thread must be recorded");
    }

    // =========================================================================
    // @Async methods — SQL on a pre-existing executor thread pool
    // =========================================================================
    // The executor and its worker thread are created before any test method
    // runs. SQL executed from that worker during a QuickPerf test must still
    // be recorded for the running test.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingExecutorThread extends SqlTestBaseJUnit5 {

        private static final ExecutorService PRE_EXISTING_EXECUTOR;
        static {
            PRE_EXISTING_EXECUTOR = Executors.newSingleThreadExecutor(
                    daemonThreadFactory("pre-existing-executor-0"));
            prestartWorkerThreads(PRE_EXISTING_EXECUTOR, 1);
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_pre_existing_executor_thread() throws Exception {
            submitSelectAndAwait(PRE_EXISTING_EXECUTOR, emf);
        }
    }

    @Test
    void sql_from_pre_existing_executor_thread_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingExecutorThread.class,
                "SQL executed on a pre-existing executor thread must be recorded");
    }

    // =========================================================================
    // CompletableFuture with a pre-existing custom executor
    // =========================================================================
    // CompletableFuture.runAsync(..., executor) wraps the user task in an
    // AsyncRun before handing it to a worker that already exists. SQL run
    // through that wrapper during a QuickPerf test must still be recorded
    // for the running test.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingCompletableFutureExecutor extends SqlTestBaseJUnit5 {

        private static final ExecutorService PRE_EXISTING_EXECUTOR;
        static {
            PRE_EXISTING_EXECUTOR = Executors.newFixedThreadPool(2,
                    daemonThreadFactory("pre-existing-cf-executor"));
            prestartWorkerThreads(PRE_EXISTING_EXECUTOR, 2);
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_via_completable_future_on_pre_existing_executor() throws Exception {
            final EntityManagerFactory localEmf = emf;
            CompletableFuture.runAsync(new Runnable() {
                @Override
                public void run() {
                    executeSelect(localEmf);
                }
            }, PRE_EXISTING_EXECUTOR).get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void sql_from_completable_future_on_pre_existing_executor_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingCompletableFutureExecutor.class,
                "SQL executed via CompletableFuture on a pre-existing executor must be recorded");
    }

    // =========================================================================
    // @Scheduled tasks — SQL on a pre-existing ScheduledExecutorService
    // =========================================================================
    // A Spring @Scheduled-style scheduler owns its worker thread for the
    // lifetime of the application. SQL executed by a scheduled task during
    // a QuickPerf test must still be recorded for the running test.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingScheduledExecutor extends SqlTestBaseJUnit5 {

        private static final ScheduledExecutorService PRE_EXISTING_SCHEDULER;
        static {
            PRE_EXISTING_SCHEDULER = Executors.newScheduledThreadPool(1,
                    daemonThreadFactory("pre-existing-scheduler-0"));
            forceSchedulerWorkerCreation(PRE_EXISTING_SCHEDULER);
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_pre_existing_scheduled_executor() throws Exception {
            scheduleSelectAndAwait(PRE_EXISTING_SCHEDULER, emf);
        }
    }

    @Test
    void sql_from_pre_existing_scheduled_executor_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingScheduledExecutor.class,
                "SQL executed on a pre-existing scheduler thread must be recorded");
    }

    // =========================================================================
    // Reactive / WebFlux schedulers (Reactor / Netty)
    // =========================================================================
    // Project Reactor schedulers and Netty event loops typically use long-lived
    // worker threads that may already exist before a test method starts.
    // When SQL runs on such workers during a QuickPerf test, QuickPerf must
    // still record that SQL for the running test.
    //
    // We simulate that lifecycle with a static executor. The workers are named
    // like Reactor bounded-elastic threads for readability only — QuickPerf
    // does not key behavior off the thread name.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingReactorScheduler extends SqlTestBaseJUnit5 {

        private static final ExecutorService REACTOR_LIKE_SCHEDULER;
        static {
            final AtomicInteger counter = new AtomicInteger();
            REACTOR_LIKE_SCHEDULER = Executors.newFixedThreadPool(2, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r,
                            "reactor-bounded-elastic-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            });
            prestartWorkerThreads(REACTOR_LIKE_SCHEDULER, 2);
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_pre_existing_reactor_scheduler() throws Exception {
            submitSelectAndAwait(REACTOR_LIKE_SCHEDULER, emf);
        }
    }

    @Test
    void sql_from_reactor_scheduler_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingReactorScheduler.class,
                "SQL executed on a pre-existing Reactor-style scheduler thread must be recorded");
    }

    // =========================================================================
    // Concurrent QuickPerf SQL recording
    // =========================================================================
    // Several @QuickPerfTest methods run at the same time in the same JVM.
    // Each method executes one SELECT and expects exactly one SELECT.
    // QuickPerf must keep each method's SQL count independent from the others.

    @QuickPerfUnderTest
    public static class ConcurrentQuickPerfTests extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void runs_select_in_parallel_method_1() {
            executeSelect(emf);
        }

        @ExpectSelect(1)
        @Test
        public void runs_select_in_parallel_method_2() {
            executeSelect(emf);
        }

        @ExpectSelect(1)
        @Test
        public void runs_select_in_parallel_method_3() {
            executeSelect(emf);
        }

        @ExpectSelect(1)
        @Test
        public void runs_select_in_parallel_method_4() {
            executeSelect(emf);
        }

        @ExpectSelect(1)
        @Test
        public void runs_select_in_parallel_method_5() {
            executeSelect(emf);
        }
    }

    @Test
    void parallel_quickperf_tests_should_record_sql_independently() {
        TestExecutionSummary summary = runInParallel(ConcurrentQuickPerfTests.class, 5);

        assertThat(summary.getTestsFailedCount())
                .as("Parallel QuickPerf tests must record SQL independently")
                .isZero();
    }

    // =========================================================================
    // Persistent worker reused across QuickPerf test lifecycles
    // =========================================================================
    // The same long-lived worker thread is used by two consecutive runs of the
    // same @QuickPerfTest method in the same JVM. Each run submits one SELECT
    // and expects exactly one SELECT.
    // QuickPerf must record the SQL for the currently running test, not keep
    // associating the worker with a previous run.

    @QuickPerfUnderTest
    public static class SqlFromPersistentSharedWorker extends SqlTestBaseJUnit5 {

        private static volatile Thread WORKER;
        private static final BlockingQueue<Runnable> TASKS = new LinkedBlockingQueue<>();

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_persistent_shared_worker() throws Exception {
            ensureWorkerStarted();
            enqueueSelectAndAwait(TASKS, emf);
        }

        private static synchronized void ensureWorkerStarted() {
            if (WORKER != null) {
                return;
            }
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            TASKS.take().run();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }, "persistent-worker");
            t.setDaemon(true);
            t.start();
            WORKER = t;
        }
    }

    @Test
    void persistent_worker_should_record_sql_across_test_lifecycles() {
        // Two consecutive runs share the same JVM and the same static worker.
        JUnit5TestsResult run1 = run(SqlFromPersistentSharedWorker.class);
        JUnit5TestsResult run2 = run(SqlFromPersistentSharedWorker.class);

        int total = run1.getNumberOfFailures() + run2.getNumberOfFailures();
        assertThat(total)
                .as("Persistent worker must record SQL for the currently running test execution, "
                        + "not a previous execution. Run 1 errors: %s | Run 2 errors: %s",
                        run1.getErrorReport(), run2.getErrorReport())
                .isZero();
    }

    // =========================================================================
    // JUnit 5 parallel execution with a shared executor
    // =========================================================================
    // Two tests run concurrently and share a static ExecutorService. QuickPerf
    // cannot tell which of the two tests owns SQL executed on the shared
    // worker, so it must not report a count for either of them: both tests
    // fail with the cross-test contamination error, including the one whose
    // SQL properties look respected. Equivalent to concurrent RANDOM_PORT
    // tests whose SQL is executed by Tomcat workers, distilled to plain Java.

    @QuickPerfUnderTest
    public static class ConcurrentTestsWithSharedExecutor extends SqlTestBaseJUnit5 {

        private static volatile ExecutorService SHARED_EXECUTOR;
        private static volatile CountDownLatch EXECUTOR_READY;
        private static volatile CountDownLatch SQL_EXECUTED_ON_SHARED_EXECUTOR;

        // Per run, so that the tests wait for each other again when they are
        // executed again in the same JVM
        @org.junit.jupiter.api.BeforeAll
        static void createLatches() {
            EXECUTOR_READY = new CountDownLatch(1);
            SQL_EXECUTED_ON_SHARED_EXECUTOR = new CountDownLatch(1);
        }

        @org.junit.jupiter.api.AfterAll
        static void shutdownSharedExecutor() {
            if (SHARED_EXECUTOR != null) {
                SHARED_EXECUTOR.shutdownNow();
                SHARED_EXECUTOR = null;
            }
        }

        @ExpectSelect(1)
        @Test
        void test_with_executor_select() throws Exception {
            // Wait until the other test has created the shared executor worker.
            assertThat(EXECUTOR_READY.await(5, TimeUnit.SECONDS)).isTrue();
            final EntityManagerFactory localEmf = emf;
            try {
                Future<?> done = submitSelect(SHARED_EXECUTOR, localEmf);
                done.get(5, TimeUnit.SECONDS);
            } finally {
                SQL_EXECUTED_ON_SHARED_EXECUTOR.countDown();
            }
        }

        @ExpectSelect(0)
        @Test
        void test_with_no_sql() throws Exception {
            // Create the worker from this test before the other test submits SQL to it.
            SHARED_EXECUTOR = Executors.newSingleThreadExecutor();
            prestartWorkerThreads(SHARED_EXECUTOR, 1);
            EXECUTOR_READY.countDown();
            // Keep this test active until the other test has run its SQL on the shared worker.
            assertThat(SQL_EXECUTED_ON_SHARED_EXECUTOR.await(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void concurrent_tests_sharing_an_executor_should_both_fail_with_cross_test_contamination() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runInParallel(ConcurrentTestsWithSharedExecutor.class, 2, failureMessages);

        // Active-set fallback attributes the shared-executor SELECT to BOTH
        // concurrent recorders and flags both of them as contaminated.
        assertThat(summary.getTestsFailedCount())
                .as("Concurrent tests sharing an executor both fail with the cross-test contamination error")
                .isEqualTo(2);

        // test_with_executor_select sees its expected 1 SELECT but still fails
        String selectTestFailure = failureMessages.of("test_with_executor_select");
        assertThat(selectTestFailure)
                .contains("a performance-related property is not respected")
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                .doesNotContain("You may think that");

        // test_with_no_sql (@ExpectSelect(0)) also sees the sibling's SELECT
        String noSqlTestFailure = failureMessages.of("test_with_no_sql");
        assertThat(noSqlTestFailure)
                .contains("2 performance-related properties are not respected")
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                .contains("You may think that <0> select statement was sent to the database");
        assertThat(noSqlTestFailure.indexOf(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE))
                .as("The contamination error is reported before the SQL property")
                .isLessThan(noSqlTestFailure.indexOf("You may think that"));
    }

    // =========================================================================
    // Cross-test contamination fails the test explicitly
    // =========================================================================
    // SQL executed on a worker thread while another test is recording cannot
    // be attributed reliably. The test fails with the cross-test contamination
    // error even if its SQL properties are respected, keeps reporting its own
    // functional failure, and stays aborted when an assumption aborts its test
    // method. The sibling test is simulated by a recorder registered from
    // another thread, so these scenarios run sequentially.

    /** Executes {@code sql} on a worker thread while the recorder built by
     * {@code siblingRecorder} on another thread is registered. */
    private static void executeOnWorkerThreadWhileRegisteredByAnotherThread(Callable<SqlRecorder> siblingRecorder,
                                                                            Runnable sql) throws Exception {
        ExecutorService siblingTestThread = Executors.newSingleThreadExecutor(daemonThreadFactory("sibling-test-thread"));
        ExecutorService workerThread = Executors.newSingleThreadExecutor(daemonThreadFactory("worker-thread"));
        try {
            final SqlRecorder registeredSiblingRecorder = siblingTestThread.submit(siblingRecorder).get(5, TimeUnit.SECONDS);
            try {
                workerThread.submit(sql).get(5, TimeUnit.SECONDS);
            } finally {
                siblingTestThread.submit(new Runnable() {
                    @Override
                    public void run() {
                        SqlRecorderRegistry.unregister(registeredSiblingRecorder);
                        registeredSiblingRecorder.cleanResources();
                    }
                }).get(5, TimeUnit.SECONDS);
            }
        } finally {
            siblingTestThread.shutdownNow();
            workerThread.shutdownNow();
        }
    }

    /** A sibling test recording its JDBC batching (@ExpectJdbcBatching). */
    private static Callable<SqlRecorder> siblingTestRecordingJdbcBatching() {
        return new Callable<SqlRecorder>() {
            @Override
            public SqlRecorder call() {
                SqlStatementBatchRecorder siblingTestRecorder = new SqlStatementBatchRecorder();
                siblingTestRecorder.startRecording(null);
                return siblingTestRecorder;
            }
        };
    }

    private static Runnable select(final EntityManagerFactory entityManagerFactory) {
        return new Runnable() {
            @Override
            public void run() {
                executeSelect(entityManagerFactory);
            }
        };
    }

    private static Runnable insert(final EntityManagerFactory entityManagerFactory) {
        return new Runnable() {
            @Override
            public void run() {
                EntityManager em = entityManagerFactory.createEntityManager();
                try {
                    em.getTransaction().begin();
                    em.createNativeQuery("INSERT INTO Book (id, title, isbn) VALUES (1000, 'title', 'isbn')")
                      .executeUpdate();
                    em.getTransaction().rollback();
                } finally {
                    em.close();
                }
            }
        };
    }

    private static void executeSelectOnWorkerThreadWhileASiblingTestIsRecording(EntityManagerFactory entityManagerFactory) throws Exception {
        executeOnWorkerThreadWhileRegisteredByAnotherThread(siblingTestRecordingJdbcBatching(), select(entityManagerFactory));
    }

    private static void executeInsertOnWorkerThreadWhileASiblingTestIsRecording(EntityManagerFactory entityManagerFactory) throws Exception {
        executeOnWorkerThreadWhileRegisteredByAnotherThread(siblingTestRecordingJdbcBatching(), insert(entityManagerFactory));
    }

    @QuickPerfUnderTest
    public static class ContaminatedTestWithRespectedSqlProperties extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @ExpectMaxSelect(5)
        @Test
        public void executes_sql_on_worker_thread_while_a_sibling_test_is_recording() throws Exception {
            executeSelectOnWorkerThreadWhileASiblingTestIsRecording(emf);
        }
    }

    @Test
    void contaminated_test_should_fail_even_if_its_sql_properties_are_respected() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runSequentially(ContaminatedTestWithRespectedSqlProperties.class, failureMessages);

        assertThat(summary.getTestsFailedCount()).isOne();
        String failure = failureMessages.of("executes_sql_on_worker_thread_while_a_sibling_test_is_recording");
        assertThat(failure)
                .as("A single contamination issue is reported for the SQL annotations sharing the recording")
                .contains("a performance-related property is not respected")
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                .doesNotContain("You may think that");

        // The contamination flag and the sibling registration do not leak into the next test
        assertNoFailure(SqlFromPreExistingExecutorThread.class,
                "SQL executed on a worker thread with a single recording test must be recorded");
    }

    @QuickPerfUnderTest
    public static class ContaminatedTestWithSqlAndJdbcBatchingIssues extends SqlTestBaseJUnit5 {

        @ExpectInsert(0)
        @ExpectJdbcBatching
        @Test
        public void executes_insert_on_worker_thread_while_a_sibling_test_is_recording() throws Exception {
            executeInsertOnWorkerThreadWhileASiblingTestIsRecording(emf);
        }
    }

    @Test
    void contamination_of_sql_and_jdbc_batching_recordings_should_be_reported_once_and_first() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runSequentially(ContaminatedTestWithSqlAndJdbcBatchingIssues.class, failureMessages);

        assertThat(summary.getTestsFailedCount()).isOne();
        String failure = failureMessages.of("executes_insert_on_worker_thread_while_a_sibling_test_is_recording");
        assertThat(failure)
                .contains("3 performance-related properties are not respected")
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                .contains("<0> but is <1>")
                .contains("JDBC batching is disabled");
        int contaminationIndex = failure.indexOf(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
        assertThat(contaminationIndex)
                .as("The contamination error is reported once, before the SQL and JDBC batching properties")
                .isEqualTo(failure.lastIndexOf(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE))
                .isLessThan(failure.indexOf("<0> but is <1>"))
                .isLessThan(failure.indexOf("JDBC batching is disabled"));
        int sqlListingIndex = failure.indexOf("[JDBC QUERY EXECUTION (executeQuery, executeBatch, ...)]");
        assertThat(contaminationIndex)
                .as("The contamination error is reported with the recorded SQL")
                .isNotNegative()
                .isLessThan(sqlListingIndex);
        assertThat(failure.indexOf("JDBC batching is disabled"))
                .as("The JDBC batching property is reported after the recorded SQL")
                .isGreaterThan(sqlListingIndex);
    }

    @QuickPerfUnderTest
    @DisableGlobalAnnotations
    public static class ContaminatedTestVerifyingOnlyJdbcBatching extends SqlTestBaseJUnit5 {

        @ExpectJdbcBatching
        @Test
        public void executes_insert_on_worker_thread_while_a_sibling_test_is_recording() throws Exception {
            executeInsertOnWorkerThreadWhileASiblingTestIsRecording(emf);
        }
    }

    @Test
    void contaminated_test_verifying_only_jdbc_batching_should_fail() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runSequentially(ContaminatedTestVerifyingOnlyJdbcBatching.class, failureMessages);

        assertThat(summary.getTestsFailedCount()).isOne();
        assertThat(failureMessages.of("executes_insert_on_worker_thread_while_a_sibling_test_is_recording"))
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
    }

    @QuickPerfUnderTest
    @DisableGlobalAnnotations
    public static class TestVerifyingOnlyJdbcBatchingWithASelectAttributedToSeveralTests extends SqlTestBaseJUnit5 {

        @ExpectJdbcBatching
        @Test
        public void executes_select_on_worker_thread_while_a_sibling_test_is_recording() throws Exception {
            executeSelectOnWorkerThreadWhileASiblingTestIsRecording(emf);
        }
    }

    @Test
    void select_attributed_to_several_tests_should_not_fail_a_test_verifying_only_jdbc_batching() {
        assertNoFailure(TestVerifyingOnlyJdbcBatchingWithASelectAttributedToSeveralTests.class,
                "A select does not change the JDBC batch sizes of the test");
    }

    @QuickPerfUnderTest
    public static class ContaminatedTestWithFunctionalFailure extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread_and_fails() throws Exception {
            executeSelectOnWorkerThreadWhileASiblingTestIsRecording(emf);
            throw new AssertionError("functional failure of the test itself");
        }
    }

    @Test
    void contaminated_test_should_keep_its_own_functional_failure() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runSequentially(ContaminatedTestWithFunctionalFailure.class, failureMessages);

        assertThat(summary.getTestsFailedCount()).isOne();
        String failure = failureMessages.of("executes_sql_on_worker_thread_and_fails");
        assertThat(failure)
                .contains("Performance-related and functional properties not respected")
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE)
                .contains("FUNCTIONAL PROPERTY")
                .contains("functional failure of the test itself");
    }

    @QuickPerfUnderTest
    public static class ContaminatedTestAbortedByAnAssumption extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread_and_is_aborted() throws Exception {
            executeSelectOnWorkerThreadWhileASiblingTestIsRecording(emf);
            Assumptions.assumeTrue(false, "assumption not respected");
        }
    }

    @Test
    void contaminated_test_aborted_by_an_assumption_should_stay_aborted() {
        TestExecutionSummary summary = runSequentially(ContaminatedTestAbortedByAnAssumption.class);

        assertThat(summary.getTestsFailedCount()).isZero();
        assertThat(summary.getTestsAbortedCount()).isOne();
    }

    // A sibling test only displaying its SQL is also a recording test.

    @QuickPerfUnderTest
    public static class ContaminatedTestWithASiblingTestDisplayingItsSql extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread_while_a_sibling_test_displays_its_sql() throws Exception {
            executeOnWorkerThreadWhileRegisteredByAnotherThread(new Callable<SqlRecorder>() {
                @Override
                public SqlRecorder call() {
                    // A sibling test annotated with @DisplaySql
                    DisplaySqlRecorder siblingTestRecorder = new DisplaySqlRecorder();
                    siblingTestRecorder.startRecording(null);
                    return siblingTestRecorder;
                }
            }, select(emf));
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread_while_a_sibling_test_displays_the_sql_of_its_method_body() throws Exception {
            executeOnWorkerThreadWhileRegisteredByAnotherThread(new Callable<SqlRecorder>() {
                @Override
                public SqlRecorder call() {
                    // A sibling test annotated with @DisplaySqlOfTestMethodBody
                    DisplaySqlOfTestMethodBodyRecorder siblingTestRecorder = new DisplaySqlOfTestMethodBodyRecorder();
                    siblingTestRecorder.startRecording(null);
                    return siblingTestRecorder;
                }
            }, select(emf));
        }
    }

    @Test
    void sibling_test_displaying_its_sql_should_make_sql_of_a_worker_thread_ambiguous() {
        FailureMessages failureMessages = new FailureMessages();
        TestExecutionSummary summary = runSequentially(ContaminatedTestWithASiblingTestDisplayingItsSql.class, failureMessages);

        assertThat(summary.getTestsFailedCount()).isEqualTo(2);
        assertThat(failureMessages.of("executes_sql_on_worker_thread_while_a_sibling_test_displays_its_sql"))
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
        assertThat(failureMessages.of("executes_sql_on_worker_thread_while_a_sibling_test_displays_the_sql_of_its_method_body"))
                .contains(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
    }

    // A DisplaySqlRecorder registers when the test execution context is built:
    // in the main JVM for a @DisplaySql test executed in a forked JVM, or on
    // another thread than the recording one (JUnit 4 timeout, ...). Until its
    // test starts recording, it does not make SQL of a worker thread ambiguous.

    @QuickPerfUnderTest
    public static class SqlOnWorkerThreadWhileADisplaySqlRecorderOfANotRecordingTestIsRegistered extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread() throws Exception {
            executeOnWorkerThreadWhileRegisteredByAnotherThread(new Callable<SqlRecorder>() {
                @Override
                public SqlRecorder call() {
                    return new DisplaySqlRecorder();
                }
            }, select(emf));
        }
    }

    @Test
    void display_sql_recorder_of_a_not_recording_test_should_not_make_sql_of_a_worker_thread_ambiguous() {
        assertNoFailure(SqlOnWorkerThreadWhileADisplaySqlRecorderOfANotRecordingTestIsRegistered.class,
                "A DisplaySqlRecorder of a test not recording must not cause a cross-test contamination failure");
    }

    @QuickPerfUnderTest
    public static class SqlOnWorkerThreadWithTheDisplaySqlRecorderOfTheTestRegisteredByAnotherThread extends SqlTestBaseJUnit5 {

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_worker_thread() throws Exception {
            // As under a JUnit 4 timeout: the DisplaySqlRecorder of the test is
            // registered by the thread building the test execution context, and
            // records with the other recorders of the test on the test thread.
            ExecutorService contextBuildingThread = Executors.newSingleThreadExecutor(daemonThreadFactory("context-building-thread"));
            ExecutorService workerThread = Executors.newSingleThreadExecutor(daemonThreadFactory("worker-thread"));
            try {
                final DisplaySqlRecorder displaySqlRecorder = contextBuildingThread.submit(new Callable<DisplaySqlRecorder>() {
                    @Override
                    public DisplaySqlRecorder call() {
                        return new DisplaySqlRecorder();
                    }
                }).get(5, TimeUnit.SECONDS);
                try {
                    displaySqlRecorder.startRecording(null);
                    submitSelectAndAwait(workerThread, emf);
                } finally {
                    displaySqlRecorder.stopRecording(null);
                    contextBuildingThread.submit(new Runnable() {
                        @Override
                        public void run() {
                            displaySqlRecorder.cleanResources();
                        }
                    }).get(5, TimeUnit.SECONDS);
                }
            } finally {
                contextBuildingThread.shutdownNow();
                workerThread.shutdownNow();
            }
        }
    }

    @Test
    void display_sql_recorder_of_the_test_registered_by_another_thread_should_not_make_sql_of_a_worker_thread_ambiguous() {
        assertNoFailure(SqlOnWorkerThreadWithTheDisplaySqlRecorderOfTheTestRegisteredByAnotherThread.class,
                "The recorders of a single test registered by several threads must not cause a cross-test contamination failure");
    }

    // =========================================================================
    // Long-running message listener thread
    // =========================================================================
    // A message listener container (Kafka, RabbitMQ, JMS, etc.) typically owns
    // long-lived worker threads that are created before a test method starts.
    // When such a worker handles a message that executes SQL during a QuickPerf
    // test, QuickPerf must still record that SQL for the running test.

    @QuickPerfUnderTest
    public static class SqlFromPreExistingMessageListenerThread extends SqlTestBaseJUnit5 {

        private static final BlockingQueue<Runnable> MESSAGE_QUEUE = new LinkedBlockingQueue<>();
        private static final Thread LISTENER_THREAD;
        static {
            LISTENER_THREAD = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            Runnable task = MESSAGE_QUEUE.take();
                            task.run();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }, "pre-existing-message-listener-0");
            LISTENER_THREAD.setDaemon(true);
            LISTENER_THREAD.start();
        }

        @ExpectSelect(1)
        @Test
        public void executes_sql_on_pre_existing_message_listener_thread() throws Exception {
            enqueueSelectAndAwait(MESSAGE_QUEUE, emf);
        }
    }

    @Test
    void sql_from_long_running_message_listener_should_be_recorded() {
        assertNoFailure(SqlFromPreExistingMessageListenerThread.class,
                "SQL executed on a pre-existing message-listener thread must be recorded");
    }

    // =========================================================================
    // Connection of a background job
    // =========================================================================
    // A background job (@Scheduled task, message listener, ...) obtains a
    // connection while a test is recording, and closes it after the end of the
    // test. The test is not responsible for this connection: it must not fail
    // with a connection leak.

    @QuickPerfUnderTest
    public static class ConnectionOfABackgroundJobClosedAfterTheTest {

        private static final DataSource DATA_SOURCE =
                aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

        private static final ExecutorService BACKGROUND_JOB_THREAD =
                Executors.newSingleThreadExecutor(daemonThreadFactory("background-job"));

        static {
            prestartWorkerThreads(BACKGROUND_JOB_THREAD, 1);
        }

        static volatile CountDownLatch jobMayCloseItsConnection;

        @ExpectNoConnectionLeak
        @Test
        public void a_background_job_obtains_a_connection_during_the_test() throws Exception {
            final CountDownLatch jobHasItsConnection = new CountDownLatch(1);
            BACKGROUND_JOB_THREAD.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try (Connection connection = DATA_SOURCE.getConnection()) {
                        jobHasItsConnection.countDown();
                        jobMayCloseItsConnection.await(5, TimeUnit.SECONDS);
                    }
                    return null;
                }
            });
            assertThat(jobHasItsConnection.await(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void connection_of_a_background_job_should_not_be_a_connection_leak_of_the_test() {
        ConnectionOfABackgroundJobClosedAfterTheTest.jobMayCloseItsConnection = new CountDownLatch(1);
        try {
            assertNoFailure(ConnectionOfABackgroundJobClosedAfterTheTest.class,
                    "A connection obtained by a background job must not be a connection leak of the test");
        } finally {
            ConnectionOfABackgroundJobClosedAfterTheTest.jobMayCloseItsConnection.countDown();
        }
    }

}
