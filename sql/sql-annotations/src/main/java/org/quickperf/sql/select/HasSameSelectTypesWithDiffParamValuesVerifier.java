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

import org.quickperf.SystemProperties;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParamValues;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.sql.select.analysis.SelectAnalysis.SameSelectTypesWithDifferentParamValues;

public class HasSameSelectTypesWithDiffParamValuesVerifier implements VerifiablePerformanceIssue<DisableSameSelectTypesWithDifferentParamValues, SelectAnalysis> {

    public static final HasSameSelectTypesWithDiffParamValuesVerifier INSTANCE = new HasSameSelectTypesWithDiffParamValuesVerifier();

    private HasSameSelectTypesWithDiffParamValuesVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(DisableSameSelectTypesWithDifferentParamValues annotation
                                   , SelectAnalysis selectAnalysis) {

        SameSelectTypesWithDifferentParamValues sameSelectTypesWithDifferentParamValues =
                selectAnalysis.getSameSelectTypesWithDifferentParamValues();

        if(sameSelectTypesWithDifferentParamValues.evaluate()) {
            String description =  "Same SELECT types with different parameter values";
            description += sameSelectTypesWithDifferentParamValues.getSuggestionToFixIt();
            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }

}
