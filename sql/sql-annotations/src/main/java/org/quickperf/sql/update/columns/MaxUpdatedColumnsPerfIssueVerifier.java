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

package org.quickperf.sql.update.columns;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectMaxUpdatedColumn;
import org.quickperf.unit.Count;

public class MaxUpdatedColumnsPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectMaxUpdatedColumn, Count> {

    public static final MaxUpdatedColumnsPerfIssueVerifier INSTANCE = new MaxUpdatedColumnsPerfIssueVerifier();

    private MaxUpdatedColumnsPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxUpdatedColumn annotation, Count maxSqlCountMeasure) {

        Count expectedCount = new Count(annotation.value());

        if(maxSqlCountMeasure.isGreaterThan(expectedCount)) {
            return buildPerfIssue(maxSqlCountMeasure, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count maxSqlCountMeasure, Count expectedCount) {
        String description =
                    "Maximum expected number of updated columns "
                  + "<" + expectedCount.getValue() + ">"
                  + " but is "
                  + "<" + maxSqlCountMeasure.getValue() + ">" + "."
                  + System.lineSeparator()
                  + System.lineSeparator()
                  + "The following requests were executed: "
                  + System.lineSeparator()
                  + maxSqlCountMeasure.getComment();
        return new PerfIssue(description);
    }

}
