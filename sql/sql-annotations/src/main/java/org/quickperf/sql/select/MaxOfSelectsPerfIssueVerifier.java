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

package org.quickperf.sql.select;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.framework.HibernateSuggestion;
import org.quickperf.sql.framework.JdbcSuggestion;
import org.quickperf.sql.framework.SqlFrameworksInClassPath;
import org.quickperf.unit.Count;

public class MaxOfSelectsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectMaxSelect, Count> {

    public static final MaxOfSelectsPerfIssueVerifier INSTANCE = new MaxOfSelectsPerfIssueVerifier();

    private MaxOfSelectsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxSelect annotation, Count measuredCount) {

        Count expectedCount = new Count(annotation.value());

         if(measuredCount.isGreaterThan(expectedCount)) {
            return buildPerfIssue(measuredCount, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count measuredCount, Count expectedCount) {

        String description = "You may think that at most <" + expectedCount.getValue() + "> select statement"
                           + (expectedCount.getValue() > 1 ? "s were" : " was" )
                           + " sent to the database"
                           + System.lineSeparator()
                           + "       " + "But in fact <" + measuredCount.getValue() + ">..."
                           + System.lineSeparator()
                           + System.lineSeparator()
                           + JdbcSuggestion.SERVER_ROUND_TRIPS.getMessage()
                           + System.lineSeparator()
                           ;

        if(SqlFrameworksInClassPath.INSTANCE.containsHibernate()) {
            String nPlusOneSelectMessage = HibernateSuggestion.N_PLUS_ONE_SELECT
                                                              .getMessage();
            description += nPlusOneSelectMessage;
        }

        return new PerfIssue(description);

    }

}