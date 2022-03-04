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
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;

import static org.quickperf.sql.SqlStatementPerfIssueBuilder.aSqlPerfIssue;

public class MaxOfSelectsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectMaxSelect, SelectAnalysis> {

    public static final MaxOfSelectsPerfIssueVerifier INSTANCE = new MaxOfSelectsPerfIssueVerifier();

    private MaxOfSelectsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxSelect annotation, SelectAnalysis selectAnalysis) {

        Count maxExpectedSelect = new Count(annotation.value());

        Count executedSelectNumber = selectAnalysis.getSelectNumber();

        if(executedSelectNumber.isGreaterThan(maxExpectedSelect)) {
            return buildPerfIssue(executedSelectNumber, maxExpectedSelect, selectAnalysis);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count measuredCount, Count expectedCount, SelectAnalysis selectAnalysis) {

        String description = aSqlPerfIssue().buildMaxOfStatementsDesc(measuredCount
                                                                    , expectedCount
                                                                    , "select");

        if (measuredCount.isGreaterThan(Count.ONE) && !selectAnalysis.hasOnlySameSelects()) {
            description += SelectAnalysis.getNPlusOneSelectAlert();
        }

        return new PerfIssue(description);

    }

}