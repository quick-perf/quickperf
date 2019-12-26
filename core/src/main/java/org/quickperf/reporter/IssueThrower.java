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

package org.quickperf.reporter;

import org.quickperf.BusinessOrTechnicalIssue;
import org.quickperf.PerfIssuesToFormat;

import java.util.Collection;

class IssueThrower {

    static final IssueThrower INSTANCE = new IssueThrower();

    private IssueThrower() {}

    void throwIfNecessary( BusinessOrTechnicalIssue businessOrTechnicalIssue
                                , Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
    ) throws Throwable {

        if (onlyBusinessIssue(businessOrTechnicalIssue, groupOfPerfIssuesToFormat)) {
            throw businessOrTechnicalIssue.getThrowable();
        }

        if (onlyPerfIssues(businessOrTechnicalIssue, groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildPerfIssuesAssertionError(groupOfPerfIssuesToFormat);
        }

        if (businessIssue(businessOrTechnicalIssue) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildFunctionalIssueAndPerfIssuesAssertionError(businessOrTechnicalIssue
                                                                                 , groupOfPerfIssuesToFormat);
        }

    }

    private boolean onlyBusinessIssue(BusinessOrTechnicalIssue businessOrTechnicalIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return businessIssue(businessOrTechnicalIssue) && !atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean businessIssue(BusinessOrTechnicalIssue businessOrTechnicalIssue) {
        return !businessOrTechnicalIssue.isNone();
    }

    private boolean onlyPerfIssues(BusinessOrTechnicalIssue businessOrTechnicalIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !businessIssue(businessOrTechnicalIssue) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean atLeastOnePerfIssue(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !groupOfPerfIssuesToFormat.isEmpty();
    }

}
