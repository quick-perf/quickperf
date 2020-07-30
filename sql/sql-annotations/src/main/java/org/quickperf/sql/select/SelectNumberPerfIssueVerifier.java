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

package org.quickperf.sql.select;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.framework.HibernateSuggestion;
import org.quickperf.sql.framework.JdbcSuggestion;
import org.quickperf.sql.framework.MicronautSuggestion;
import org.quickperf.sql.framework.SqlFrameworksInClassPath;
import org.quickperf.unit.Count;

import static org.quickperf.unit.Count.*;

public class SelectNumberPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectSelect, Count> {

    public static final SelectNumberPerfIssueVerifier INSTANCE = new SelectNumberPerfIssueVerifier();

    private SelectNumberPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectSelect annotation, Count measuredCount) {

        Count expectedCount = new Count(annotation.value());

        if (!measuredCount.isEqualTo(expectedCount)) {
            return buildPerfIssue(measuredCount, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count measuredCount, Count expectedCount) {

        if(measuredCount.isGreaterThan(expectedCount) && measuredCount.isGreaterThan(ONE)) {
            String description = buildDescriptionWithRoundTripsAndPossiblyNPlusOneSelect(measuredCount
                                                                                       , expectedCount);
            return new PerfIssue(description);
        }

        String description = buildBaseDescription(measuredCount, expectedCount);
        return new PerfIssue(description);

    }

    private String buildBaseDescription(Count measuredCount, Count expectedCount) {
        return "You may think that <" + expectedCount.getValue() + "> select statement"
               + (expectedCount.getValue() > 1 ? "s were" : " was" )
               + " sent to the database"
               + System.lineSeparator()
               + "       " + "But in fact <" + measuredCount.getValue() + ">...";
    }

    private String buildDescriptionWithRoundTripsAndPossiblyNPlusOneSelect(Count measuredCount, Count expectedCount) {
        
        String description = buildBaseDescription(measuredCount, expectedCount);

        description += System.lineSeparator()
                     + System.lineSeparator()
                     + JdbcSuggestion.SERVER_ROUND_TRIPS.getMessage();

        if(SqlFrameworksInClassPath.INSTANCE.containsHibernate()) {
            String hibernateNPlusOneSelectMessage = HibernateSuggestion.N_PLUS_ONE_SELECT
                                          .getMessage();
            description += System.lineSeparator() + hibernateNPlusOneSelectMessage;
        }

        if(SqlFrameworksInClassPath.INSTANCE.containsMicronaut()) {
            String micronautNPlusOneSelectMessage = MicronautSuggestion.N_PLUS_ONE_SELECT
                                          .getMessage();
            description += System.lineSeparator() + micronautNPlusOneSelectMessage;
        }
        
        return description;
    
    }

}
