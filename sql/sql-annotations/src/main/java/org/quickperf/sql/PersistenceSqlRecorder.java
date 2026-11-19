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

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.sql.repository.SqlRepository;
import org.quickperf.sql.repository.SqlRepositoryFactory;

import java.util.List;


public class PersistenceSqlRecorder implements SqlRecorder<SqlExecutions> {

    private final DataSourceProxyVerifier datasourceProxyVerifier = new DataSourceProxyVerifier();

    private SqlRepository sqlRepository;

    // True when SQL was recorded while it was attributed to several recording
    // tests: the recorded SQL may include SQL of another test. Set by the
    // threads recording SQL, read by findRecord.
    private volatile boolean crossTestContamination;

    private SqlExecutions record;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        // Publish last: fully initialise sqlRepository BEFORE register,
        // so a worker thread that takes the active-set fallback during this
        // construction window cannot observe `this` mid-construction and NPE
        // on a still-null sqlRepository.
        sqlRepository = SqlRepositoryFactory.getSqlRepository(testExecutionContext);
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries, int listenerIdentifier) {
        // Before adding the execution, see findRecord
        if (SqlRecorderRegistry.isSqlOfCurrentThreadAttributedToSeveralTests()) {
            crossTestContamination = true;
        }
        datasourceProxyVerifier.addListenerIdentifier(listenerIdentifier);
        sqlRepository.addQueryExecution(execInfo, queries);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.unregister(this);
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        sqlRepository.flush(workingFolder);
        if(datasourceProxyVerifier.hasQuickPerfBuiltSeveralDataSourceProxies()) {
            System.out.println();
            System.err.println(DataSourceProxyVerifier.SEVERAL_PROXIES_WARNING);
        }
    }

    // A worker thread that found this recorder before the test stopped
    // recording can still add SQL after. The record is therefore a copy of
    // the executions, made on the first call: the SQL properties of the test
    // are verified, and its SQL displayed, from the same executions. The flag
    // is read after the copy: a thread sets it before adding its execution,
    // so it covers every copied execution attributed to several tests.
    @Override
    public SqlExecutions findRecord(TestExecutionContext testExecutionContext) {
        if (record != null) {
            return record;
        }
        // Test executed in a specific JVM
        if (sqlRepository == null) {
            sqlRepository = SqlRepositoryFactory.getSqlRepository(testExecutionContext);
        }
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        SqlExecutions executions = sqlRepository.findExecutedQueries(workingFolder).copy();
        if (crossTestContamination) {
            // SqlExecutions.verifyRecord() turns the tag into a perf issue
            // failing the test.
            executions.markCrossTestContamination();
        }
        record = executions;
        return record;
    }

    @Override
    public void cleanResources() {
        // The thread recording the SQL of a timed-out test can keep running,
        // for example in a JDBC call ignoring the interrupt, and never stop
        // recording. Without this, the test would stay a recording test, and
        // the SQL of the worker threads of the next tests would be attributed
        // to several tests. Unregistering again has no effect.
        SqlRecorderRegistry.unregister(this);
    }

}
