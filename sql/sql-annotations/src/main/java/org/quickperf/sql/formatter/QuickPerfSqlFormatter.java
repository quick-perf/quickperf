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
package org.quickperf.sql.formatter;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import org.quickperf.sql.SqlFormatter;
import org.quickperf.sql.framework.ClassPath;

import java.util.List;

public class QuickPerfSqlFormatter {

    public static final QuickPerfSqlFormatter INSTANCE = new QuickPerfSqlFormatter();

    private static final QuickPerfQueryLogEntryCreator PRETTY_QUERY_LOG_ENTRY_CREATOR = new QuickPerfQueryLogEntryCreator();

    private static class QuickPerfQueryLogEntryCreator extends DefaultQueryLogEntryCreator {

        @Override
        protected String formatQuery(String query) {
            return QuickPerfSqlFormatter.SQL_FORMATTER.formatQuery(query);
        }

    }

    private QuickPerfSqlFormatter() { }

    public String format(ExecutionInfo executionInfo, List<QueryInfo> queries) {
        final boolean writeDataSourceName = false;
        final boolean writeConnectionId = false;
        return PRETTY_QUERY_LOG_ENTRY_CREATOR.getLogEntry(executionInfo
                                                        , queries
                                                        , writeDataSourceName
                                                        , writeConnectionId);
    }

    private static final SqlFormatter SQL_FORMATTER = buildSqlFormatter();

    private static SqlFormatter buildSqlFormatter() {

        SqlFormatter sqlFormatterDefinedByUser =
                SqlFormatterDefinedByUserRetriever.INSTANCE.retrieveSqlFormatterDefinedByUser();

        if (!sqlFormatterDefinedByUser.equals(SqlFormatter.NONE)) {
            return sqlFormatterDefinedByUser;
        }

        if (ClassPath.INSTANCE.containsHibernate()) {
            return SqlFormatterBasedOnHibernate.INSTANCE;
        }

        return SqlFormatter.NONE;

    }

}
