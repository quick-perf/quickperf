/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

public class ConnectionListenerRegistry {

    public static final ConnectionListenerRegistry INSTANCE = new ConnectionListenerRegistry();

    private final Collection<ConnectionListener> connectionListenersOfTestJvm = new ArrayList<>();

    private static final ThreadLocal<Map<Class<? extends ConnectionListener>, ConnectionListener>> CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM =
            new InheritableThreadLocal<Map<Class<? extends ConnectionListener>, ConnectionListener>>() {
        @Override
        protected Map<Class<? extends ConnectionListener>, ConnectionListener> initialValue() {
            return new HashMap<>();
        }
    };

    private ConnectionListenerRegistry() { }

    public void register(ConnectionListener connectionListener) {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            connectionListenersOfTestJvm.add(connectionListener);
        } else {
            Map<Class<? extends ConnectionListener>, ConnectionListener> connectionListenerByType
                    = CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM.get();
            connectionListenerByType.put(connectionListener.getClass(), connectionListener);
        }
    }

    public static void unregister(ConnectionListener connectionListener) {
        if(!TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Map<Class<? extends ConnectionListener>, ConnectionListener> connectionListenerByType
                    = CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM.get();
            connectionListenerByType.remove(connectionListener);
        }
    }

    public Collection<ConnectionListener> getConnectionListeners() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return connectionListenersOfTestJvm;
        }
        Map<Class<? extends ConnectionListener>, ConnectionListener> connectionListenerByType
                = CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM.get();
        return connectionListenerByType.values();
    }

    public void clear() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            connectionListenersOfTestJvm.clear();
        }
        CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM.remove();
    }

    public <T extends ConnectionListener> T getConnectionListenerOfType(Class<T> type) {
        Map<Class<? extends ConnectionListener>, ConnectionListener> connectionListenerByType
                = CONNECTION_LISTENER_BY_TYPE_WHEN_ONE_JVM.get();
        return type.cast(connectionListenerByType.get(type));
    }

}
