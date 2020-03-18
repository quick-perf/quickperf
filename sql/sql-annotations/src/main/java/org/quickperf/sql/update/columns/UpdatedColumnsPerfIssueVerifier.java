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

package org.quickperf.sql.update.columns;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectUpdatedColumn;

public class UpdatedColumnsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectUpdatedColumn, NumberOfUpdatedColumnsStatisticsMeasure> {

    public static final UpdatedColumnsPerfIssueVerifier INSTANCE = new UpdatedColumnsPerfIssueVerifier();

    private UpdatedColumnsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectUpdatedColumn annotation, NumberOfUpdatedColumnsStatisticsMeasure measure) {

        NumberOfUpdatedColumnsStatistics updatedColumnsStatistics = measure.getValue();

        int expectedUpdatesColumns = annotation.value();

        long maxColumnCount = updatedColumnsStatistics.getMax();
        long minColumnCount = updatedColumnsStatistics.getMin();

        if(  expectedUpdatesColumns != minColumnCount || expectedUpdatesColumns != maxColumnCount
          ) {
            return buildPerfIssue(expectedUpdatesColumns, maxColumnCount, minColumnCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(int expectedUpdatesColumns, long maxColumnCount, long minColumnCount) {

        String assertionMessage = "Expected number of updated columns "
                                + "<" + expectedUpdatesColumns + ">";

        boolean oneColumnUpdated = minColumnCount == maxColumnCount;
        if(oneColumnUpdated) {
            assertionMessage += " but is " + "<" + minColumnCount + ">" + ".";
        } else {
            assertionMessage += " but is between " + "<" + minColumnCount + ">"
                              + " and " + "<" + maxColumnCount +  ">.";
        }

        return new PerfIssue(assertionMessage);

    }

}