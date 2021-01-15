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

package org.quickperf.sql.select;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;

public class SelectNumberPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectSelect, SelectAnalysis> {

    public static final SelectNumberPerfIssueVerifier INSTANCE = new SelectNumberPerfIssueVerifier();

    private SelectNumberPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectSelect annotation, SelectAnalysis selectAnalysis) {

        Count expectedSelectNumber = new Count(annotation.value());

        Count executedSelectNumber = selectAnalysis.getSelectNumber();

        if (!executedSelectNumber.isEqualTo(expectedSelectNumber)) {
            return buildPerfIssue(executedSelectNumber, expectedSelectNumber, selectAnalysis);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count executedSelectNumber, Count expectedSelectNumber, SelectAnalysis selectAnalysis) {

        String description = buildBaseDescription(executedSelectNumber, expectedSelectNumber);

        if(   executedSelectNumber.isGreaterThan(Count.ONE)
           && executedSelectNumber.isGreaterThan(expectedSelectNumber)
           && !selectAnalysis.hasOnlySameSelects()
          ) {
            description += SelectAnalysis.getSuggestionToFIxNPlusOneSelect();
        }

        return new PerfIssue(description);

    }

    private String buildBaseDescription(Count measuredCount, Count expectedCount) {
        boolean severalExpectedStatements = expectedCount.getValue() > 1;
        boolean severalExecutedStatements = measuredCount.getValue() > 1;
        return "You may think that <" + expectedCount.getValue() + "> select statement"
               + (severalExpectedStatements ? "s were" : " was" )
               + " sent to the database"
               + System.lineSeparator()
               + "       " + "But there " + (severalExecutedStatements ? "are" : "is" ) + " in fact <" + measuredCount.getValue() + ">...";
    }

}
