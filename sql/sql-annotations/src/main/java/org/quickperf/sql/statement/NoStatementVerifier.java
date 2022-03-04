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
package org.quickperf.sql.statement;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.annotation.DisableStatements;

public class NoStatementVerifier implements VerifiablePerformanceIssue<DisableStatements, BooleanMeasure> {

    public static final NoStatementVerifier INSTANCE = new NoStatementVerifier();

    private NoStatementVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(DisableStatements annotation, BooleanMeasure noStatementExist) {

        Boolean noStatement = noStatementExist.getValue();
        if(noStatement) {
            return PerfIssue.NONE;
        }

        String description = "At least one Statement. Only PreparedStatement and CallableStatement were expected.";
        return new PerfIssue(description);

    }

}
