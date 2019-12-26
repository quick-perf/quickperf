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

package org.quickperf.sql.select.columns;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxSelectedColumn;
import org.quickperf.unit.Count;

public class MaxSelectedColumnsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectMaxSelectedColumn, Count> {

    public static final MaxSelectedColumnsPerfIssueVerifier INSTANCE = new MaxSelectedColumnsPerfIssueVerifier();

    private MaxSelectedColumnsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxSelectedColumn annotation, Count maxSqlCountMeasure) {

        Count expectedCount = new Count(annotation.value());

        if(maxSqlCountMeasure.isGreaterThan(expectedCount)) {
            return buildPerfIssue(maxSqlCountMeasure, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count maxSqlCountMeasure, Count expectedCount) {
        String description =
                    "Maximum expected number of selected columns "
                  + "<" + expectedCount.getValue() + ">"
                  + " but is "
                  + "<" + maxSqlCountMeasure.getValue() + ">" + "."
                  + maxSqlCountMeasure.getComment();
        return new PerfIssue(description);
    }

}
