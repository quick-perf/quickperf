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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.sql.display;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.TestExecutionContext;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;
import org.quickperf.sql.formatter.QuickPerfSqlFormatter;

import java.util.List;

public class DisplaySqlRecorder implements SqlRecorder<SqlExecutions> {

    public DisplaySqlRecorder() {
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {}

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {}

    @Override
    public SqlExecutions findRecord(TestExecutionContext testExecutionContext) {
        return SqlExecutions.NONE;
    }

    @Override
    public void cleanResources() {
        SqlRecorderRegistry.unregister(this);
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries) {
        String sqlQueries = QuickPerfSqlFormatter.INSTANCE.format(execInfo, queries);
        System.out.println(sqlQueries);
    }

}
