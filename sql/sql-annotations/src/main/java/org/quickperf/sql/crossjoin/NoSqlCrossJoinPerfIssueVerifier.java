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

package org.quickperf.sql.crossjoin;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.annotation.DisableCrossJoin;

public class NoSqlCrossJoinPerfIssueVerifier implements VerifiablePerformanceIssue<DisableCrossJoin, BooleanMeasure> {

    public static final NoSqlCrossJoinPerfIssueVerifier INSTANCE = new NoSqlCrossJoinPerfIssueVerifier();

    private NoSqlCrossJoinPerfIssueVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(DisableCrossJoin annotation, BooleanMeasure hasSqlCrossJoinMeasure) {

        if(hasSqlCrossJoinMeasure.getValue()) {
            return new PerfIssue("cross join detected");
        }

        return PerfIssue.NONE;

    }

}
