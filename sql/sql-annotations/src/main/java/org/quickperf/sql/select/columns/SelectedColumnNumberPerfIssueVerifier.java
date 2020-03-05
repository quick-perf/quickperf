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

package org.quickperf.sql.select.columns;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectSelectedColumn;
import org.quickperf.unit.Count;

public class SelectedColumnNumberPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectSelectedColumn, Count> {

    public static final SelectedColumnNumberPerfIssueVerifier INSTANCE = new SelectedColumnNumberPerfIssueVerifier();

    private SelectedColumnNumberPerfIssueVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectSelectedColumn annotation, Count measuredCount) {

        Count expectedCount = new Count(annotation.value());

        if (!measuredCount.isEqualTo(expectedCount)) {
            return buildPerfIssue(measuredCount, expectedCount);
        }

        return PerfIssue.NONE;

    }

    private PerfIssue buildPerfIssue(Count measuredCount, Count expectedCount) {

        String description = "You may think that <" + expectedCount.getValue() + "> column"
                + (expectedCount.getValue() > 1 ? "s were" : " was" )
                + " selected"
                + System.lineSeparator()
                + "       " + "But in fact <" + measuredCount.getValue() + ">..."
                ;

        return new PerfIssue(description);

    }

}
