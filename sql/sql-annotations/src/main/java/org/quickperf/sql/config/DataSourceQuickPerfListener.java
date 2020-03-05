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

package org.quickperf.sql.config;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

class DataSourceQuickPerfListener implements QueryExecutionListener {

    private final int listenerIdentifier = ThreadLocalRandom.current().nextInt();

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) {}


    @Override
    @SuppressWarnings("unchecked")
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) {

        Collection<SqlRecorder> sqlRecorders = SqlRecorderRegistry.INSTANCE.getSqlRecorders();

        for (SqlRecorder sqlRecorder : sqlRecorders) {
            sqlRecorder.addQueryExecution(executionInfo, queries, listenerIdentifier);
        }

    }

}