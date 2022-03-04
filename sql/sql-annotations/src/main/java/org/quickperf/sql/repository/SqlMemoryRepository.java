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
package org.quickperf.sql.repository;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.WorkingFolder;
import org.quickperf.sql.SqlExecutions;

import java.util.List;

class SqlMemoryRepository implements SqlRepository{

    private SqlExecutions sqlExecutions = new SqlExecutions();

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries) {
        sqlExecutions.add(execInfo, queries);
    }

    @Override
    public void flush(WorkingFolder working) { }

    @Override
    public SqlExecutions findExecutedQueries(WorkingFolder workingFolder) {
        return sqlExecutions;
    }

    public void saveSqlExecutions(SqlExecutions sqlExecutions) {
        this.sqlExecutions = sqlExecutions;
    }

}
