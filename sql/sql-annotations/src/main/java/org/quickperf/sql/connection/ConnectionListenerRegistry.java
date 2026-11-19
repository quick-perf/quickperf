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

import org.quickperf.perfrecording.TestThreads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

/*
 * Mirror of {@link org.quickperf.sql.SqlRecorderRegistry}'s dual-map design.
 * Same per-thread fast path + process-wide active-set fallback. See that
 * class's Javadoc for the full rationale.
 *
 * Differences from SqlRecorderRegistry:
 *  - A ConnectionLeakListener is not in the active set: a worker thread does
 *    not notify it. A test leaks a connection when one of its own threads
 *    keeps it open. A connection that another thread obtains while the test
 *    is recording, and closes after, belongs to a background job (@Scheduled
 *    task, message listener, ...), or to another test in a parallel test
 *    suite: it would make the test fail with a connection leak that is not
 *    its own.
 *  - The other listeners, such as the profiler of @ProfileConnection, only
 *    display the connection events, so they have no cross-test contamination
 *    detection. A connection event of a worker thread goes to each of them,
 *    those of a sibling test included.
 */
public class ConnectionListenerRegistry {

    public static final ConnectionListenerRegistry INSTANCE = new ConnectionListenerRegistry();

    // Forked-JVM branch: thread-safe, since it is read on every JDBC
    // operation, possibly by worker threads, and written only at test setup /
    // teardown.
    private final List<ConnectionListener> connectionListenersOfTestJvm
            = new CopyOnWriteArrayList<ConnectionListener>();

    // Single-JVM branch: per-thread fast path, the listeners registered by the
    // current thread. A ThreadLocal released when the thread ends, as in
    // SqlRecorderRegistry.
    private static final ThreadLocal<ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener>>
            PER_THREAD_LISTENERS = new ThreadLocal<ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener>>();

    // Process-wide identity set of every live listener notified of the
    // connection events of worker threads; iterated by worker threads on the
    // JDBC hot path, written only on register / unregister. Not used in a
    // forked JVM, where every thread finds the listeners in
    // connectionListenersOfTestJvm.
    private static final Set<ConnectionListener> ACTIVE_LISTENERS
            = Collections.newSetFromMap(new ConcurrentHashMap<ConnectionListener, Boolean>());

    private ConnectionListenerRegistry() { }

    public void register(ConnectionListener connectionListener) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            connectionListenersOfTestJvm.add(connectionListener);
            return;
        }
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = listenersOfCurrentThread();
        // Per-thread map first, ACTIVE_LISTENERS last, as in
        // SqlRecorderRegistry.
        ConnectionListener displaced = perThread.put(connectionListener.getClass(), connectionListener);
        if (displaced != null && displaced != connectionListener) {
            // A listener its test never unregistered: without this, worker
            // threads would keep notifying it until the JVM exits.
            ACTIVE_LISTENERS.remove(displaced);
        }
        if (isNotifiedByWorkerThreads(connectionListener)) {
            ACTIVE_LISTENERS.add(connectionListener);
        }
    }

    // See the class comment.
    private static boolean isNotifiedByWorkerThreads(ConnectionListener connectionListener) {
        return !(connectionListener instanceof ConnectionLeakListener);
    }

    /**
     * Makes a listener registered by another thread also a listener of the
     * current thread, without changing the listeners found by the other
     * threads. Mirror of
     * {@link org.quickperf.sql.SqlRecorderRegistry#addToRecordersOfCurrentThread}.
     *
     * @return {@code true} if the listener was added, {@code false} if it
     * already was a listener of the current thread
     */
    public boolean addToListenersOfCurrentThread(ConnectionListener connectionListener) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // The listeners of a forked JVM are not per thread.
            return false;
        }
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = listenersOfCurrentThread();
        return perThread.put(connectionListener.getClass(), connectionListener) != connectionListener;
    }

    private static ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> listenersOfCurrentThread() {
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = PER_THREAD_LISTENERS.get();
        if (perThread == null) {
            perThread = new ConcurrentHashMap<Class<? extends ConnectionListener>, ConnectionListener>();
            PER_THREAD_LISTENERS.set(perThread);
        }
        return perThread;
    }

    /**
     * Undoes {@link #addToListenersOfCurrentThread(ConnectionListener)}.
     * Leaves the listeners found by the other threads unchanged.
     */
    public void removeFromListenersOfCurrentThread(ConnectionListener connectionListener) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return;
        }
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = PER_THREAD_LISTENERS.get();
        if (perThread != null) {
            // The map stays, possibly empty: the thread remains a test thread.
            perThread.remove(connectionListener.getClass(), connectionListener);
        }
    }

    public static void unregister(ConnectionListener connectionListener) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // Forked-JVM unregister does NOT remove the listener from
            // connectionListenersOfTestJvm: in forked-JVM mode, lifecycle
            // teardown such as @After's emf.close() runs AFTER stopRecording()
            // saves the captured data, but before the JVM exits. Removing
            // the listener here would silently change runtime behaviour
            // observed in pre-existing tests.
            return;
        }
        // ACTIVE_LISTENERS first, per-thread map last, so that worker threads
        // stop finding the listener as soon as its test stops recording.
        ACTIVE_LISTENERS.remove(connectionListener);
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = PER_THREAD_LISTENERS.get();
        if (perThread != null) {
            perThread.remove(connectionListener.getClass(), connectionListener);
            // Intentionally NOT removing the empty map from
            // PER_THREAD_LISTENERS: same discipline as SqlRecorderRegistry.
            // The empty map acts as a "this is a test pool thread"
            // sentinel so the next test's @Before SQL on the same Surefire
            // pool thread takes the test-thread fast path (returning the
            // empty map's values) instead of falling through to the
            // worker-thread broadcast fallback. The map is released when its
            // thread ends.
        }
    }

    public Collection<ConnectionListener> getConnectionListeners() {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // CopyOnWriteArrayList iterators are snapshot-based; safe to
            // return the live reference.
            return connectionListenersOfTestJvm;
        }
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = PER_THREAD_LISTENERS.get();
        if (perThread == null && TestThreads.isCurrentThreadMarked()) {
            // A test thread whose test has not registered any listener yet.
            perThread = listenersOfCurrentThread();
        }
        if (perThread != null) {
            // Test-thread fast path. Empty map is the post-clear /
            // post-unregister sentinel - do NOT fall through to the
            // active-set fallback.
            // A connection notifies the listeners returned when it is
            // obtained, for its whole life: a live view of the map, so that it
            // also notifies the listeners registered later on this thread, for
            // example the profiler of a test using a connection obtained in
            // @BeforeClass. A view of a ConcurrentHashMap can be iterated
            // while the map changes.
            return Collections.unmodifiableCollection(perThread.values());
        }
        // Worker-thread fallback: defensive copy avoids
        // ConcurrentModificationException against concurrent register /
        // unregister on the JDBC hot path.
        return new ArrayList<ConnectionListener>(ACTIVE_LISTENERS);
    }

    public <T extends ConnectionListener> T getConnectionListenerOfType(Class<T> type) {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            for (ConnectionListener candidate : connectionListenersOfTestJvm) {
                if (type.isInstance(candidate)) {
                    return type.cast(candidate);
                }
            }
            return null;
        }
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = PER_THREAD_LISTENERS.get();
        if (perThread != null) {
            // Test-thread branch. Return null on a type miss WITHOUT
            // falling through to the worker fallback.
            ConnectionListener hit = perThread.get(type);
            return hit != null ? type.cast(hit) : null;
        }
        if (TestThreads.isCurrentThreadMarked()) {
            // A test thread whose test has not registered any listener yet.
            return null;
        }
        // Worker-thread fallback.
        for (ConnectionListener candidate : ACTIVE_LISTENERS) {
            if (type.isInstance(candidate)) {
                return type.cast(candidate);
            }
        }
        return null;
    }

    public void clear() {
        if (TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            connectionListenersOfTestJvm.clear();
            return;
        }
        // Empty-marker sentinel - same discipline as SqlRecorderRegistry.
        ConcurrentMap<Class<? extends ConnectionListener>, ConnectionListener> perThread
                = listenersOfCurrentThread();
        for (ConnectionListener listener : perThread.values()) {
            ACTIVE_LISTENERS.remove(listener);
        }
        // Cleared, not replaced: the connections obtained by this thread keep
        // a view of this map.
        perThread.clear();
    }

}
