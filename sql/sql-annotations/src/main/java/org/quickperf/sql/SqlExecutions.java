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

package org.quickperf.sql;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.PerfIssue;
import org.quickperf.PerfIssuesFormat;
import org.quickperf.perfrecording.ViewablePerfRecordIfPerfIssue;

import java.io.Serializable;
import java.util.*;

public class SqlExecutions implements Iterable<SqlExecution>, ViewablePerfRecordIfPerfIssue, Serializable {

    public static final SqlExecutions NONE = new SqlExecutions();

    private final Deque<SqlExecution> sqlExecutions = new ArrayDeque<>();

    public void add(ExecutionInfo execInfo, List<QueryInfo> queries) {
        SqlExecution sqlExecution = new SqlExecution(execInfo, queries);
        sqlExecutions.addLast(sqlExecution);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SqlExecution sqlExecution : sqlExecutions) {
            sb.append("\t").append(sqlExecution.toString());
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public boolean oneExecutionHasQueryRespecting(SqlExecutionPredicate predicate) {
        for (SqlExecution sqlExecution : sqlExecutions) {
            if (predicate.test(sqlExecution)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this == NONE || sqlExecutions.isEmpty() ;
    }

    public long retrieveQueryNumberOfType(QueryType queryType) {
        long queryNumber = 0;
        QueryTypeRetriever queryTypeRetriever = QueryTypeRetriever.INSTANCE;
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (queryType.equals(queryTypeRetriever.typeOf(query))) {
                    queryNumber++;
                }
            }
        }
        return queryNumber;
    }

    public long getMaxNumberOfColumns() {
        long maxNumberOfColumnsForAllExecs = 0;
        for (SqlExecution sqlExecution : sqlExecutions) {
            long columnCount = sqlExecution.getColumnCount();
            if(columnCount > maxNumberOfColumnsForAllExecs) {
                maxNumberOfColumnsForAllExecs = columnCount;
            }
        }
        return maxNumberOfColumnsForAllExecs;
    }

    @Override
    public String format(Collection<PerfIssue> perfIssues) {
        String standardFormatting = PerfIssuesFormat.STANDARD.format(perfIssues);
        return    standardFormatting
                + System.lineSeparator()
                + System.lineSeparator()
                + "[SQL EXECUTIONS]"
                + System.lineSeparator()
                + toString();
    }

    @Override
    public Iterator<SqlExecution> iterator() {
        return sqlExecutions.iterator();
    }
}
