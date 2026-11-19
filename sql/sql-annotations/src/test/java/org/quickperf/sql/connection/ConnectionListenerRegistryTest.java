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
import org.quickperf.perfrecording.TestThreads;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConnectionListenerRegistryTest {

    @After
    public void tearDown() {
        // Surefire parallel=all: clear so this test never leaks into siblings.
        ConnectionListenerRegistry.INSTANCE.clear();
    }

    @Test public void
    should_get_a_connection_listener_from_its_type() {

        // GIVEN
        ConnectionLeakListener connectionLeakListener = new ConnectionLeakListener();
        ConnectionListenerRegistry.INSTANCE.register(connectionLeakListener);

        // WHEN
        ConnectionListener retrievedConnectionListener = ConnectionListenerRegistry.INSTANCE
                                                         .getConnectionListenerOfType(ConnectionLeakListener.class);

        // THEN
        assertThat(retrievedConnectionListener).isEqualTo(connectionLeakListener);

    }

    @Test public void
    should_clear_connection_listener_registry() {

        // GIVEN
        ConnectionListener aConnectionListener = mock(ConnectionListener.class);
        ConnectionListenerRegistry.INSTANCE.register(aConnectionListener);

        // WHEN
        ConnectionListenerRegistry.INSTANCE.clear();

        // THEN
        Collection<ConnectionListener> connectionListeners = ConnectionListenerRegistry.INSTANCE.getConnectionListeners();
        assertThat(connectionListeners).hasSize(0);

    }

    @Test public void
    should_notify_a_connection_listener_on_worker_threads() throws InterruptedException {

        // GIVEN
        ConnectionListener connectionListener = new AConnectionListener();
        ConnectionListenerRegistry.INSTANCE.register(connectionListener);

        // WHEN
        Collection<ConnectionListener> listenersOfAWorkerThread = getConnectionListenersOfAWorkerThread();

        // THEN
        assertThat(listenersOfAWorkerThread).contains(connectionListener);

    }

    @Test public void
    should_not_notify_a_connection_leak_listener_on_worker_threads() throws InterruptedException {

        // GIVEN: a test recording its connection leaks, whose connections are
        // obtained on its own thread
        ConnectionLeakListener connectionLeakListener = new ConnectionLeakListener();
        ConnectionListenerRegistry.INSTANCE.register(connectionLeakListener);

        // WHEN: a worker thread, such as a background job, obtains a connection
        Collection<ConnectionListener> listenersOfAWorkerThread = getConnectionListenersOfAWorkerThread();

        // THEN: the test is not responsible for this connection
        assertThat(listenersOfAWorkerThread).doesNotContain(connectionLeakListener);

    }

    @Test public void
    should_not_notify_a_replaced_connection_listener_on_worker_threads() throws InterruptedException {

        // GIVEN: a listener never unregistered, as after a test that leaked
        // it, then a listener of the same class registered on the same thread
        ConnectionListener leakedListener = new AConnectionListener();
        ConnectionListenerRegistry.INSTANCE.register(leakedListener);
        ConnectionListener replacingListener = new AConnectionListener();
        ConnectionListenerRegistry.INSTANCE.register(replacingListener);

        // WHEN
        Collection<ConnectionListener> listenersOfAWorkerThread = getConnectionListenersOfAWorkerThread();

        // THEN: not an exact check, as the tests run in parallel may register
        // listeners at the same time
        assertThat(listenersOfAWorkerThread).contains(replacingListener)
                                            .doesNotContain(leakedListener);

    }

    @Test public void
    should_notify_a_connection_listener_registered_after_the_connection_was_obtained() {

        // GIVEN a connection obtained by a test thread before its test
        // registers a listener, for example in @BeforeClass
        ConnectionListenerRegistry.INSTANCE.clear();
        Collection<ConnectionListener> listenersOfTheConnection = ConnectionListenerRegistry.INSTANCE.getConnectionListeners();

        // WHEN
        ConnectionListener connectionListener = new AConnectionListener();
        ConnectionListenerRegistry.INSTANCE.register(connectionListener);

        // THEN
        assertThat(listenersOfTheConnection).containsExactly(connectionListener);

    }

    @Test public void
    should_notify_a_connection_listener_registered_after_the_connection_was_obtained_by_a_test_thread_without_listeners() throws InterruptedException {

        // GIVEN a thread marked as a test thread, whose test registered no
        // listener when the connection was obtained
        final AtomicReference<Collection<ConnectionListener>> listenersOfTheConnection
                = new AtomicReference<Collection<ConnectionListener>>();
        final ConnectionListener connectionListener = new AConnectionListener();
        Thread testThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TestThreads.markCurrentThread();
                listenersOfTheConnection.set(ConnectionListenerRegistry.INSTANCE.getConnectionListeners());

                // WHEN
                ConnectionListenerRegistry.INSTANCE.register(connectionListener);
            }
        }, "test-thread");
        testThread.start();
        testThread.join();

        // THEN
        assertThat(listenersOfTheConnection.get()).containsExactly(connectionListener);
        ConnectionListenerRegistry.unregister(connectionListener);

    }

    @Test public void
    should_not_notify_a_connection_listener_unregistered_after_the_connection_was_obtained() {

        // GIVEN
        ConnectionListener connectionListener = new AConnectionListener();
        ConnectionListenerRegistry.INSTANCE.register(connectionListener);
        Collection<ConnectionListener> listenersOfTheConnection = ConnectionListenerRegistry.INSTANCE.getConnectionListeners();

        // WHEN
        ConnectionListenerRegistry.unregister(connectionListener);

        // THEN
        assertThat(listenersOfTheConnection).doesNotContain(connectionListener);

    }

    private static class AConnectionListener extends ConnectionListener { }

    private static Collection<ConnectionListener> getConnectionListenersOfAWorkerThread() throws InterruptedException {
        final AtomicReference<Collection<ConnectionListener>> listeners
                = new AtomicReference<Collection<ConnectionListener>>();
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                listeners.set(ConnectionListenerRegistry.INSTANCE.getConnectionListeners());
            }
        }, "connection-listener-registry-worker");
        worker.start();
        worker.join();
        return listeners.get();
    }

}