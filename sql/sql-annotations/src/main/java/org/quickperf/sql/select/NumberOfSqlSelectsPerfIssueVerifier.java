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
package org.quickperf.sql.select;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectSelects;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;

import java.util.Arrays;

import static org.quickperf.sql.SqlStatementPerfIssueBuilder.aSqlPerfIssue;

public class NumberOfSqlSelectsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectSelects, SelectAnalysis> {

    public static final NumberOfSqlSelectsPerfIssueVerifier INSTANCE = new NumberOfSqlSelectsPerfIssueVerifier();

    private NumberOfSqlSelectsPerfIssueVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(final ExpectSelects annotation, final SelectAnalysis analysis) {

        int sum = 0;
        for (ExpectSelect expectSelect : annotation.value()) {
            sum += expectSelect.value();
        }
        final Count expectedCount = new Count(sum);

        final Count measuredCount = analysis.getSelectNumber();
        if (!measuredCount.isEqualTo(expectedCount)) {
            return aSqlPerfIssue().buildNotEqualNumberOfStatements(
                    measuredCount, expectedCount, "SELECT");
        }

        return PerfIssue.NONE;

    }

}