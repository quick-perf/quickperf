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

package org.quickperf.sql.delete;

import org.quickperf.PerfIssue;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectDelete;
import org.quickperf.unit.Count;

import static org.quickperf.sql.SqlRequestPerfIssueBuilder.aSqlPerfIssue;

public class NumberOfSqlDeletePerfIssueVerifier implements VerifiablePerformanceIssue<ExpectDelete, Count> {

    private static final String DELETE = "DELETE";

    public static final NumberOfSqlDeletePerfIssueVerifier INSTANCE = new NumberOfSqlDeletePerfIssueVerifier();

    private NumberOfSqlDeletePerfIssueVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(ExpectDelete annotation, Count measuredCount) {

        Count expectedCount = new Count(annotation.value());

        if (!measuredCount.isEqualTo(expectedCount)) {
            return aSqlPerfIssue().buildNotEqualNumberOfRequests(measuredCount
                                       , expectedCount
                                       , DELETE);
        }

        return PerfIssue.NONE;

    }

}
