/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.like;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.annotation.DisableLikeWithLeadingWildcard;

public class HasLikeWithLeadingWildcardVerifier implements VerifiablePerformanceIssue<DisableLikeWithLeadingWildcard, BooleanMeasure> {

    public static final HasLikeWithLeadingWildcardVerifier INSTANCE = new HasLikeWithLeadingWildcardVerifier();

    private HasLikeWithLeadingWildcardVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(DisableLikeWithLeadingWildcard annotation, BooleanMeasure likeWithLeadingWildcardMeasure) {

        if(likeWithLeadingWildcardMeasure.getValue()) {
            return new PerfIssue("Like with leading wildcard detected (% or _)");
        }

        return PerfIssue.NONE;

    }
}
