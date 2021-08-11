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

import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ConnectionListenerRegistryTest {


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

}