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

import org.quickperf.perfrecording.TestThreads;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

/*
 * Dual-map registry rationale
 * ---------------------------
 * QuickPerf historically attributed a SQL execution to a test via
 * InheritableThreadLocal: the test thread registered a recorder and
 * child threads inherited it. That model breaks in practice because
 * the SQL of a realistic Spring / Jakarta / Quarkus / Micronaut app is
 * almost never executed on a *child* of the test thread - it runs on
 * threads from pre-existing pools (Tomcat HTTP workers, @Async and
 * @Scheduled executors, JMS / Kafka message listeners, virtual
 * threads, gRPC handlers, Reactor schedulers, ...). Those threads have
 * no inheritance relation to the test thread, so they observed null
 * and the SQL was silently dropped.
 *
 * We therefore look up recorders along TWO axes:
 *
 *   1) PER_THREAD_RECORDERS  ::  ThreadLocal, recorders by class.
 *      Fast-path for the test thread itself. Guarantees parallel-test
 *      isolation: under Surefire `parallel=all, threadCount=5`, each
 *      test's assertions only ever see its own recorders, never a
 *      neighbour test's.
 *
 *   2) ACTIVE_RECORDERS  ::  process-wide identity set of every
 *      recorder currently live (between register() and unregister()).
 *      Worker-thread fallback: a Tomcat / @Async / virtual / gRPC
 *      thread executing SQL on behalf of a test has no
 *      PER_THREAD_RECORDERS map (it is not the test thread), so
 *      we fall back to this map and the SQL is still attributed to a
 *      live recorder. The map's value is the registering test thread's
 *      id (the "owner tid"); it is the contamination-detection axis.
 *
 * Why we need BOTH, and why neither alone is enough:
 *
 *   - Per-thread map only  =>  regression to the InheritableThreadLocal
 *     era: pool-owned worker threads observe nothing, SQL is silently
 *     lost, and assertions like @ExpectSelect see 0 instead of N.
 *   - Active set only      =>  parallel tests cross-contaminate: a
 *     worker thread from test A would observe test B's recorders with
 *     no way to disambiguate, producing wrong counts under
 *     `parallel=all`.
 *
 * The dual-map design gives the test thread a clean, isolated fast
 * path while still letting unrelated worker threads find the recorders
 * they need to record SQL against.
 */
public class SqlRecorderRegistry {

    public static final SqlRecorderRegistry INSTANCE = new SqlRecorderRegistry();

    // Forked-JVM branch: thread-safe so workers in the forked JVM can read it
    // safely while the test thread mutates it during register / unregister.
    private final ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> sqlRecorderByTypeOfTestJvm
            = new ConcurrentHashMap<Class<? extends SqlRecorder>, SqlRecorder>();

    // Single-JVM branch: per-thread fast path, the recorders registered by the
    // current thread. Only ever read and written by its own thread. A
    // ThreadLocal, so that the map of a thread is released when the thread
    // ends; not inheritable, so that a thread started by the test takes the
    // worker-thread fallback.
    private static final ThreadLocal<ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder>>
            PER_THREAD_RECORDERS = new ThreadLocal<ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder>>();

    // Process-wide: every live recorder mapped to its owner test thread id.
    // Used by worker-thread fallback paths when PER_THREAD_RECORDERS has no
    // map for the calling thread. Not used in a forked JVM, where every
    // thread finds the recorders in sqlRecorderByTypeOfTestJvm.
    private static final ConcurrentMap<SqlRecorder, Long> ACTIVE_RECORDERS
            = new ConcurrentHashMap<SqlRecorder, Long>();

    // TRUE when the last getSqlRecorders() of the current thread fell back to
    // the recorders of several recording tests: the SQL being recorded by the
    // thread is broadcast to tests it may not belong to. The recorders read
    // it when they record this SQL, see
    // isSqlOfCurrentThreadAttributedToSeveralTests().
    private static final ThreadLocal<Boolean> SQL_ATTRIBUTED_TO_SEVERAL_TESTS = new ThreadLocal<Boolean>();

    private SqlRecorderRegistry() {}

    public void register(SqlRecorder sqlRecorder) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            sqlRecorderByTypeOfTestJvm.put(sqlRecorder.getClass(), sqlRecorder);
            return;
        }
        Long tid = Long.valueOf(Thread.currentThread().getId());
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread == null) {
            perThread = new ConcurrentHashMap<Class<? extends SqlRecorder>, SqlRecorder>();
            PER_THREAD_RECORDERS.set(perThread);
        }
        // Per-thread map first, ACTIVE_RECORDERS last, so that the registering
        // thread never finds its recorder in the active set while its own fast
        // path still misses it.
        SqlRecorder displaced = perThread.put(sqlRecorder.getClass(), sqlRecorder);
        if (displaced != null && displaced != sqlRecorder) {
            // Avoid leaking the displaced recorder in the active set.
            ACTIVE_RECORDERS.remove(displaced);
        }
        ACTIVE_RECORDERS.put(sqlRecorder, tid);
    }

    /**
     * Makes a recorder registered by another thread also a recorder of the
     * current thread, without changing the recorders found by the other
     * threads. For a recorder registered when the test execution context is
     * built, while the test method is executed on another thread (JUnit 4
     * timeout, JUnit 5 {@code @Timeout} on a separate thread, ...): this
     * thread takes the test-thread fast path, which only returns its own
     * recorders.
     *
     * @return {@code true} if the recorder was added, {@code false} if it
     * already was a recorder of the current thread
     */
    public boolean addToRecordersOfCurrentThread(SqlRecorder sqlRecorder) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // The recorders of a forked JVM are not per thread.
            return false;
        }
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread == null) {
            perThread = new ConcurrentHashMap<Class<? extends SqlRecorder>, SqlRecorder>();
            PER_THREAD_RECORDERS.set(perThread);
        }
        return perThread.put(sqlRecorder.getClass(), sqlRecorder) != sqlRecorder;
    }

    /**
     * Undoes {@link #addToRecordersOfCurrentThread(SqlRecorder)}. Leaves the
     * recorders found by the other threads unchanged.
     */
    public void removeFromRecordersOfCurrentThread(SqlRecorder sqlRecorder) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return;
        }
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread != null) {
            // The map stays, possibly empty: the thread remains a test thread.
            perThread.remove(sqlRecorder.getClass(), sqlRecorder);
        }
    }

    public static void unregister(SqlRecorder sqlRecorder) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // Forked-JVM unregister does NOT remove the recorder from
            // sqlRecorderByTypeOfTestJvm: in forked-JVM mode, lifecycle
            // teardown such as @After's emf.close() and Hibernate's
            // SchemaExport drop both run AFTER stopRecording() saves the
            // captured data, but before the JVM exits. They emit additional
            // SQL through the still-attached datasource-proxy listeners,
            // and the assertion (e.g. @ExpectJdbcBatching) is evaluated in
            // the OUTER JVM from the serialized file written by save() -
            // so leaving the recorder registered here is a no-op for
            // assertion correctness, while removing it would silently
            // change runtime behaviour observed in pre-existing tests.
            return;
        }
        // ACTIVE_RECORDERS first, per-thread map last, so that worker threads
        // stop finding the recorder as soon as its test stops recording.
        ACTIVE_RECORDERS.remove(sqlRecorder);
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread != null) {
            perThread.remove(sqlRecorder.getClass(), sqlRecorder);
            // Intentionally NOT removing the empty map from PER_THREAD_RECORDERS:
            // the map acts as a "this is a test pool thread" sentinel so that
            // the next test's @Before SQL on this same Surefire pool thread takes
            // the test-thread fast path (returning the empty map's values)
            // instead of falling through to the worker-thread broadcast fallback,
            // which would otherwise contaminate sibling tests' recorders under
            // Surefire parallel=all. The map is released when its thread ends,
            // so the threads started for a single test (JUnit 4 timeout, JUnit 5
            // @Timeout on a separate thread, TestNG timeOut, ...) leave nothing
            // behind.
        }
    }

    public Collection<SqlRecorder> getSqlRecorders() {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return sqlRecorderByTypeOfTestJvm.values();
        }
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread != null) {
            // Test-thread fast path. Returning here even on an empty map
            // (post-clear() sentinel) keeps the test thread off the worker
            // fallback so it cannot observe sibling tests' recorders under
            // Surefire parallel=all.
            setSqlOfCurrentThreadAttributedToSeveralTests(false);
            return new ArrayList<SqlRecorder>(perThread.values());
        }
        if (TestThreads.isCurrentThreadMarked()) {
            // A test thread whose test has not registered any recorder yet.
            setSqlOfCurrentThreadAttributedToSeveralTests(false);
            return new ArrayList<SqlRecorder>();
        }
        // Worker-thread fallback: the recorders of the tests recording SQL at
        // that time (weakly-consistent snapshot of ACTIVE_RECORDERS). If two
        // or more distinct tests are simultaneously recording the attribution
        // is genuinely ambiguous: the recorders recording this SQL make every
        // test owning one of them fail with the cross-test contamination
        // error.
        Map<SqlRecorder, Long> recordingThreadByRecorder = recordingThreadByRecorderOfRecordingTests();
        setSqlOfCurrentThreadAttributedToSeveralTests(hasSeveralRecordingThreads(recordingThreadByRecorder));
        return new ArrayList<SqlRecorder>(recordingThreadByRecorder.keySet());
    }

    public <T extends SqlRecorder> T getSqlRecorderOfType(Class<T> type) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return type.cast(sqlRecorderByTypeOfTestJvm.get(type));
        }
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> perThread = PER_THREAD_RECORDERS.get();
        if (perThread != null) {
            // Test-thread branch. Return null on a type miss WITHOUT
            // falling through to the worker fallback - a test thread
            // that has registered something (or holds the post-clear()
            // sentinel) MUST NOT observe sibling tests' recorders.
            SqlRecorder hit = perThread.get(type);
            return hit != null ? type.cast(hit) : null;
        }
        if (TestThreads.isCurrentThreadMarked()) {
            // A test thread whose test has not registered any recorder yet.
            return null;
        }
        // Worker-thread fallback: the first matching recorder of a test
        // recording SQL. This lookup records no SQL: unlike getSqlRecorders,
        // it leaves the attribution of the SQL of the current thread
        // unchanged.
        for (SqlRecorder candidate : recordingThreadByRecorderOfRecordingTests().keySet()) {
            if (type.isInstance(candidate)) {
                return type.cast(candidate);
            }
        }
        return null;
    }

    // The active recorders of the tests recording SQL, with the id of the
    // thread recording it.
    private static Map<SqlRecorder, Long> recordingThreadByRecorderOfRecordingTests() {
        Map<SqlRecorder, Long> recordingThreadByRecorder = new HashMap<SqlRecorder, Long>();
        for (Map.Entry<SqlRecorder, Long> entry : ACTIVE_RECORDERS.entrySet()) {
            Long recordingThread = recordingThreadOf(entry.getKey(), entry.getValue());
            if (recordingThread != null) {
                recordingThreadByRecorder.put(entry.getKey(), recordingThread);
            }
        }
        return recordingThreadByRecorder;
    }

    private static boolean hasSeveralRecordingThreads(Map<SqlRecorder, Long> recordingThreadByRecorder) {
        return new HashSet<Long>(recordingThreadByRecorder.values()).size() >= 2;
    }

    // Id of the thread recording the SQL of the test owning the recorder, or
    // null if this test is not recording. The recorders register when their
    // test starts recording, on the recording thread, and unregister when it
    // stops, except DisplaySqlRecorder: it registers when the test execution
    // context is built - in this JVM even for a test executed in a forked JVM,
    // possibly on another thread than the recording one (JUnit 4 timeout,
    // ...) - and unregisters in cleanResources(). Until its test starts
    // recording, and after, it only displays the SQL of the threads of its
    // test, not the SQL of the worker threads.
    private static Long recordingThreadOf(SqlRecorder recorder, Long registrationThread) {
        if (recorder instanceof DisplaySqlRecorder) {
            return ((DisplaySqlRecorder) recorder).getRecordingThreadId();
        }
        return registrationThread;
    }

    static void setSqlOfCurrentThreadAttributedToSeveralTests(boolean attributedToSeveralTests) {
        if (attributedToSeveralTests) {
            SQL_ATTRIBUTED_TO_SEVERAL_TESTS.set(Boolean.TRUE);
        } else if (SQL_ATTRIBUTED_TO_SEVERAL_TESTS.get() != null) {
            SQL_ATTRIBUTED_TO_SEVERAL_TESTS.remove();
        }
    }

    /**
     * Tells whether the SQL the current thread is recording is attributed to
     * several recording tests, so possibly to tests it does not belong to.
     * Recorders call it while recording SQL: the attribution is the one of
     * the last {@link #getSqlRecorders()} call of the current thread, which
     * returned the recorders of this SQL.
     */
    public static boolean isSqlOfCurrentThreadAttributedToSeveralTests() {
        return Boolean.TRUE.equals(SQL_ATTRIBUTED_TO_SEVERAL_TESTS.get());
    }

    public void clear() {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            sqlRecorderByTypeOfTestJvm.clear();
            return;
        }
        ConcurrentMap<Class<? extends SqlRecorder>, SqlRecorder> previous = PER_THREAD_RECORDERS.get();
        if (previous != null) {
            for (SqlRecorder r : previous.values()) {
                ACTIVE_RECORDERS.remove(r);
            }
        }
        // Install an empty-marker map so subsequent reads from THIS thread
        // (registry tests' "register; clear; read" pattern) still observe
        // perThread != null and DO NOT fall through to the worker-thread
        // fallback. Subsequent register(...) on this thread finds the empty
        // map and reuses it.
        PER_THREAD_RECORDERS.set(new ConcurrentHashMap<Class<? extends SqlRecorder>, SqlRecorder>());
    }

}
