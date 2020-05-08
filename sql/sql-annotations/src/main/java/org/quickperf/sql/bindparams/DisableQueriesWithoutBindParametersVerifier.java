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

package org.quickperf.sql.bindparams;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters;

public class DisableQueriesWithoutBindParametersVerifier implements VerifiablePerformanceIssue<DisableQueriesWithoutBindParameters, BooleanMeasure> {

    public static final DisableQueriesWithoutBindParametersVerifier INSTANCE = new DisableQueriesWithoutBindParametersVerifier();

    private DisableQueriesWithoutBindParametersVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(final DisableQueriesWithoutBindParameters annotation, final BooleanMeasure areAllParametersBound) {

        if(!areAllParametersBound.getValue()) {
            return new PerfIssue("Unbound parameters detected");
        }

        return PerfIssue.NONE;

    }

}
