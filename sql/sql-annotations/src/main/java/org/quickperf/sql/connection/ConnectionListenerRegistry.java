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

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

public class ConnectionListenerRegistry {

    public static final ConnectionListenerRegistry INSTANCE = new ConnectionListenerRegistry();

    private final Collection<ConnectionsListener> connectionsListenersOfTestJvm = new ArrayList<>();

    private static final InheritableThreadLocal<Collection<ConnectionsListener>> CONNECTION_LISTENERS_WHEN_ONE_JVM = new InheritableThreadLocal<Collection<ConnectionsListener>>() {
        @Override
        protected Collection<ConnectionsListener> initialValue() {
            return new ArrayList<>();
        }
    };

    private ConnectionListenerRegistry() { }

    public void register(ConnectionsListener connectionsListener) {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            connectionsListenersOfTestJvm.add(connectionsListener);
        } else {
            Collection<ConnectionsListener> connectionsListeners = CONNECTION_LISTENERS_WHEN_ONE_JVM.get();
            connectionsListeners.add(connectionsListener);
        }
    }

    public static void unregister(ConnectionsListener connectionsListener) {
        if(!TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Collection<ConnectionsListener> sqlRecorders = CONNECTION_LISTENERS_WHEN_ONE_JVM.get();
            sqlRecorders.remove(connectionsListener);
        }
    }

    public Collection<ConnectionsListener> getConnectionListeners() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return connectionsListenersOfTestJvm;
        }
        return CONNECTION_LISTENERS_WHEN_ONE_JVM.get();
    }

}
