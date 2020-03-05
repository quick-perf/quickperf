/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.sql.display;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.TestExecutionContext;
import org.quickperf.sql.DataSourceProxyVerifier;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.formatter.QuickPerfSqlFormatter;

import java.util.List;

public class DisplaySqlOfTestMethodBodyRecorder implements SqlRecorder<SqlExecutions> {

    private DataSourceProxyVerifier datasourceProxyVerifier = new DataSourceProxyVerifier();

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.unregister(this);
        if(datasourceProxyVerifier.hasQuickPerfBuiltSeveralDataSourceProxies()) {
            System.out.println();
            System.out.println(DataSourceProxyVerifier.SEVERAL_PROXIES_WARNING);
        }
        System.out.println();
        System.out.println("Use @" + DisplaySql.class.getSimpleName() + " to also see queries before and after test execution.");
    }

    @Override
    public SqlExecutions findRecord(TestExecutionContext testExecutionContext) {
        return SqlExecutions.NONE;
    }

    @Override
    public void cleanResources() {}

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries, int listenerIdentifier) {
        datasourceProxyVerifier.addListenerIdentifier(listenerIdentifier);
        String sqlQueries = QuickPerfSqlFormatter.INSTANCE.format(execInfo, queries);
        System.out.println(sqlQueries);
    }

}