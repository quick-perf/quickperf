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

package org.quickperf.sql.analyze;

import net.ttddyy.dsproxy.QueryType;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.bindparams.AllParametersAreBoundExtractor;
import org.quickperf.sql.execution.SqlAnalysis;
import org.quickperf.sql.like.ContainsLikeWithLeadingWildcardExtractor;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.time.ExecutionTime;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class SqlReport {

    private static final String HEADER = System.lineSeparator() + "[QUICK PERF] SQL ANALYSIS" + System.lineSeparator();
    private static final String EMPTY_MESSAGE = "";
    private static final String DISPLAY_SEPARATOR = "                                            * * * * *";
    private static final String ALERT_MESSAGE = "!!! WARNING !!!";
    private static final String QUERY_WITHOUT_BIND_PARAMETERS = "- Query without bind parameters";

    public static SqlReport INSTANCE = new SqlReport();

    private SqlReport() {
    }

    public void writeReport(PrintWriter writer, SqlAnalysis sqlAnalysis){
        SqlExecutions sqlExecutions = sqlAnalysis.getSqlExecutions();
        writer.write(HEADER);
        writer.write(buildJdbcExecutions(sqlExecutions));
        writer.write(getMaxTime(sqlExecutions));
        writer.write(buildSelectMessages(sqlAnalysis));
        writer.write(buildNPlusOneMessage(sqlAnalysis));
        writer.write(buildInsertMessage(sqlExecutions));
        writer.write(buildUpdateMessage(sqlExecutions));
        writer.write(buildDeleteMessage(sqlExecutions));
        writer.write(formatQueries(sqlExecutions));
    }

    private String buildSelectMessages(SqlAnalysis sqlAnalysis) {
        if (numberOf(sqlAnalysis.getSqlExecutions(), QueryType.SELECT) == 0){
            return EMPTY_MESSAGE;
        }

        SelectAnalysis selectAnalysis = sqlAnalysis.getSelectAnalysis();
        SqlExecutions selectExecutions = sqlAnalysis.getSqlExecutions().filterByQueryType(QueryType.SELECT);
        int selectCount = numberOf(sqlAnalysis.getSqlExecutions(), QueryType.SELECT);
        String mes = buildSelectCountReport(selectCount);

        if (selectAnalysis.hasSameSelects()) {
            mes += "- Same SELECT statements" + System.lineSeparator();
        }
        if (checkIfWildcard(selectExecutions)) {
            mes += "- Like with leading wildcard detected (% or _)" + System.lineSeparator();
        }
        if (checkIfBindParameters(selectExecutions)) {
            mes += QUERY_WITHOUT_BIND_PARAMETERS + System.lineSeparator();
        }

        return mes;
    }

    private int numberOf(SqlExecutions sqlExecutions, QueryType queryType) {
        return sqlExecutions.retrieveQueryNumberOfType(queryType);
    }

    private String buildNPlusOneMessage(SqlAnalysis sqlAnalysis) {
        if (sqlAnalysis.getSelectAnalysis().getSameSelectTypesWithDifferentParamValues().evaluate()) {
          return this.addSeparationString() + ALERT_MESSAGE
                    + sqlAnalysis.getSelectAnalysis().getSameSelectTypesWithDifferentParamValues().getSuggestionToFixIt()
                    + System.lineSeparator()
                    + System.lineSeparator();
        }

        return EMPTY_MESSAGE;
    }

    private String formatQueries(SqlExecutions sqlExecutions) {
        if (sqlExecutions.getNumberOfExecutions() == 0) {
            return EMPTY_MESSAGE;
        }

        return sqlExecutions.getNumberOfExecutions() > 1 ? this.addSeparationString() + "QUERIES " + System.lineSeparator()
                + sqlExecutions.toString() : this.addSeparationString() + "QUERY " + System.lineSeparator() + sqlExecutions.toString();
    }

    private String buildJdbcExecutions(SqlExecutions sqlExecutions) {
        return this.addSeparationString() + "SQL EXECUTIONS: " + sqlExecutions.getNumberOfExecutions() + System.lineSeparator();
    }

    private String buildUpdateCountReport(int updateCount) {
        if (updateCount > 0) {
            return this.addSeparationString() + QueryType.UPDATE + ": " + updateCount + System.lineSeparator();
        }

        return EMPTY_MESSAGE;
    }

    private String buildInsertCountReport(int insertCount) {
        if (insertCount > 0) {
            return this.addSeparationString() + QueryType.INSERT + ": " + insertCount + System.lineSeparator();
        }

        return EMPTY_MESSAGE;
    }

    private String buildInsertMessage(SqlExecutions sqlExecutions) {
        if (numberOf(sqlExecutions, QueryType.INSERT) == 0){
            return EMPTY_MESSAGE;
        }

        int insertCount = numberOf(sqlExecutions, QueryType.INSERT);
        String mes = buildInsertCountReport(insertCount);
        SqlExecutions insertExecutions = sqlExecutions.filterByQueryType(QueryType.INSERT);

        if (checkIfBindParameters(insertExecutions)) {
            mes += QUERY_WITHOUT_BIND_PARAMETERS + System.lineSeparator();
        }

        return mes;
    }

    private String buildUpdateMessage(SqlExecutions sqlExecutions) {
        if (numberOf(sqlExecutions, QueryType.UPDATE) == 0) {
            return EMPTY_MESSAGE;
        }

        SqlExecutions updateExecutions = sqlExecutions.filterByQueryType(QueryType.UPDATE);
        int updateCount = numberOf(sqlExecutions, QueryType.UPDATE);
        String mes = buildUpdateCountReport(updateCount);

        if (checkIfBindParameters(updateExecutions)) {
            mes += QUERY_WITHOUT_BIND_PARAMETERS + System.lineSeparator();
        }

        return mes;
    }

    private String buildDeleteMessage(SqlExecutions sqlExecutions) {
        if (numberOf(sqlExecutions, QueryType.DELETE) == 0) {
            return EMPTY_MESSAGE;
        }

        SqlExecutions updateExecutions = sqlExecutions.filterByQueryType(QueryType.DELETE);
        int deleteCount = numberOf(sqlExecutions, QueryType.DELETE);
        String mes = buildDeleteCountReport(deleteCount);

        if (checkIfBindParameters(updateExecutions)) {
            mes += QUERY_WITHOUT_BIND_PARAMETERS + System.lineSeparator();
        }

        return mes;
    }

    private String buildSelectCountReport(int selectCount) {
        if (selectCount > 0) {
            return this.addSeparationString() + QueryType.SELECT + ": " + selectCount + System.lineSeparator();
        }

        return EMPTY_MESSAGE;
    }

    private String buildDeleteCountReport(int deleteCount) {
        if (deleteCount > 0) {
            return this.addSeparationString() + QueryType.DELETE + ": " + deleteCount + System.lineSeparator();
        }
        return EMPTY_MESSAGE;
    }

    private String getMaxTime(SqlExecutions sqlExecutions) {
        if (sqlExecutions.getNumberOfExecutions() == 0) {
            return EMPTY_MESSAGE;
        }

        long maxExecutionTime = 0;

        for (SqlExecution execution : sqlExecutions) {
            long executionTime = execution.getElapsedTime();

            if (executionTime > maxExecutionTime) {
                maxExecutionTime = executionTime;
            }
        }

        return "MAX TIME: " + new ExecutionTime(maxExecutionTime, TimeUnit.MILLISECONDS).toString() + System.lineSeparator();
    }

    private boolean checkIfWildcard(SqlExecutions sqlExecutions) {
        return ContainsLikeWithLeadingWildcardExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions).getValue();
    }

    private boolean checkIfBindParameters(SqlExecutions sqlExecutions) {
        return !AllParametersAreBoundExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions).getValue();
    }

    private String addSeparationString() {
        return DISPLAY_SEPARATOR + System.lineSeparator();
    }

}