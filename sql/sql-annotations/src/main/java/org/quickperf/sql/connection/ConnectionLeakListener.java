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

import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.repository.BooleanMeasureRepository;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ConnectionLeakListener extends ConnectionListener
        implements RecordablePerformance<BooleanMeasure> {

    private final List<Connection> connections = new ArrayList<>();

    private BooleanMeasure connectionLeak;

    private static final String CONNECTION_LEAK_FILE_NAME = "connection-leak.ser";

    @Override
    public void theDatasourceGetsTheConnection(Connection connection) {
        connections.add(connection);
    }

    @Override
    public void close(Connection connection) {
        connections.remove(connection);
    }

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        connections.clear();
        ConnectionListenerRegistry.INSTANCE.register(this);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        ConnectionListenerRegistry.unregister(this);
        connectionLeak = BooleanMeasure.of(!connections.isEmpty());
        connections.clear();
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            BooleanMeasureRepository.INSTANCE.save(connectionLeak, workingFolder, CONNECTION_LEAK_FILE_NAME);
        }
    }

    @Override
    public BooleanMeasure findRecord(TestExecutionContext testExecutionContext) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            return BooleanMeasureRepository.INSTANCE.find(workingFolder, CONNECTION_LEAK_FILE_NAME);
        }
        return connectionLeak;
    }

    @Override
    public void cleanResources() { }

}
