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

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.INSTANCE.register(this);
        sqlRepository = SqlRepositoryFactory.getSqlRepository(testExecutionContext);
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries, int listenerIdentifier) {
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

    @Override
    public SqlExecutions findRecord(TestExecutionContext testExecutionContext) {
        // Test executed in a specific JVM
        if (sqlRepository == null) {
            sqlRepository = SqlRepositoryFactory.getSqlRepository(testExecutionContext);
        }
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        return sqlRepository.findExecutedQueries(workingFolder);
    }

    @Override
    public void cleanResources() {}

}
