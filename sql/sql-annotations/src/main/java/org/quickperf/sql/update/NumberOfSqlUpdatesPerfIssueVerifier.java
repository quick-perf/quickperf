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
package org.quickperf.sql.update;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.quickperf.sql.annotation.ExpectUpdates;
import org.quickperf.unit.Count;

import java.util.Arrays;

import static org.quickperf.sql.SqlStatementPerfIssueBuilder.aSqlPerfIssue;

public class NumberOfSqlUpdatesPerfIssueVerifier implements VerifiablePerformanceIssue<ExpectUpdates, Count> {

    public static final NumberOfSqlUpdatesPerfIssueVerifier INSTANCE = new NumberOfSqlUpdatesPerfIssueVerifier();

    private NumberOfSqlUpdatesPerfIssueVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(final ExpectUpdates annotation, final Count measuredCount) {

        int sum = 0;
        for (ExpectUpdate expectUpdate : annotation.value()) {
            sum += expectUpdate.value();
        }
        final Count expectedCount = new Count(sum);

        if (!measuredCount.isEqualTo(expectedCount)) {
            return aSqlPerfIssue().buildNotEqualNumberOfStatements(
                    measuredCount, expectedCount, "UPDATE");
        }

        return PerfIssue.NONE;

    }

}