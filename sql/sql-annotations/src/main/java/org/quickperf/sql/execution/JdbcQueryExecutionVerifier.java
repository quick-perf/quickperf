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
package org.quickperf.sql.execution;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectJdbcQueryExecution;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;

public class JdbcQueryExecutionVerifier implements VerifiablePerformanceIssue<ExpectJdbcQueryExecution, SqlAnalysis> {

    public static final JdbcQueryExecutionVerifier INSTANCE = new JdbcQueryExecutionVerifier();

    private JdbcQueryExecutionVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(ExpectJdbcQueryExecution annotation, SqlAnalysis sqlAnalysis) {

        Count expectedExecutionNumber = new Count(annotation.value());
        Count executionNumber = sqlAnalysis.getJdbcQueryExecutionNumber();

        if (!executionNumber.isEqualTo(expectedExecutionNumber)) {
            String description =   buildBaseDescription(executionNumber, expectedExecutionNumber)
                                 + buildPotentialSuggestionToFix(sqlAnalysis, executionNumber
                                                               , expectedExecutionNumber);
            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }

    private String buildBaseDescription(Count executionNumber, Count expectedExecutionNumber) {
        boolean severalExpectedExecutions = expectedExecutionNumber.getValue() > 1;
        boolean severalExecutions = executionNumber.getValue() > 1;
        return    "You may think that there " + (severalExpectedExecutions ? "were" : "was")
                + " <" + expectedExecutionNumber.getValue() + ">"
                + " JDBC query execution" + (severalExpectedExecutions ? "s" : "" ) + " (execute, executeQuery, executeBatch, ...)"
                + System.lineSeparator()
                + "       " + "But there " + (severalExecutions ? "are" : "is") + " <" + executionNumber.getValue() + ">...";
    }

    private String buildPotentialSuggestionToFix(SqlAnalysis sqlAnalysis, Count executionNumber, Count expectedExecutionNumber) {
        SelectAnalysis selectAnalysis = sqlAnalysis.getSelectAnalysis();
        SelectAnalysis.SameSelectTypesWithDifferentParamValues sameSelectTypesWithDifferentParamValues =
                selectAnalysis.getSameSelectTypesWithDifferentParamValues();
        if(   executionNumber.isGreaterThan(expectedExecutionNumber)
           && sameSelectTypesWithDifferentParamValues.evaluate()
        ) {
            return sameSelectTypesWithDifferentParamValues.getSuggestionToFixIt();
        }
        return "";
    }

}
