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
package org.quickperf.sql.display;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.TestExecutionContext;
import org.quickperf.perfrecording.ThreadBoundPerfRecorder;
import org.quickperf.sql.DataSourceProxyVerifier;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;
import org.quickperf.sql.formatter.QuickPerfSqlFormatter;

import java.util.List;

public class DisplaySqlRecorder implements SqlRecorder<SqlExecutions>, ThreadBoundPerfRecorder {

    private final DataSourceProxyVerifier datasourceProxyVerifier = new DataSourceProxyVerifier();

    // Registered when the test execution context is built, to also display
    // the SQL executed before and after the test method: its registration
    // does not tell that its test is recording SQL. Id of the thread
    // recording the SQL of the test while it is recording, null otherwise.
    private volatile Long recordingThreadId;

    // True while this recorder is also a recorder of the recording thread,
    // because another thread registered it. Only used by the recording thread.
    private boolean addedToRecordingThread;

    public DisplaySqlRecorder() {
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    // The code executed before and after the test method may be executed on
    // another thread than the one that registered this recorder (JUnit 4
    // Timeout rule). cleanResources(), on the same thread, unbinds it.
    @Override
    public void bindToCurrentThread() {
        SqlRecorderRegistry.INSTANCE.addToRecordersOfCurrentThread(this);
    }

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        recordingThreadId = Thread.currentThread().getId();
        // The test method may be executed on another thread than the one that
        // registered this recorder (JUnit 4 timeout, JUnit 5 @Timeout on a
        // separate thread): make it a recorder of the recording thread too.
        addedToRecordingThread = SqlRecorderRegistry.INSTANCE.addToRecordersOfCurrentThread(this);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        if (addedToRecordingThread) {
            SqlRecorderRegistry.INSTANCE.removeFromRecordersOfCurrentThread(this);
            addedToRecordingThread = false;
        }
        recordingThreadId = null;
    }

    public Long getRecordingThreadId() {
        return recordingThreadId;
    }

    @Override
    public SqlExecutions findRecord(TestExecutionContext testExecutionContext) {
        return SqlExecutions.NONE;
    }

    @Override
    public void cleanResources() {
        if(datasourceProxyVerifier.hasQuickPerfBuiltSeveralDataSourceProxies()) {
            System.out.println();
            System.out.println(DataSourceProxyVerifier.SEVERAL_PROXIES_WARNING);
            System.out.println();
        }
        SqlRecorderRegistry.unregister(this);
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries, int listenerIdentifier) {
        String sqlQueries = QuickPerfSqlFormatter.INSTANCE.format(execInfo, queries);
        System.out.println(sqlQueries);
        datasourceProxyVerifier.addListenerIdentifier(listenerIdentifier);
    }

}
