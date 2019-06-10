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

package org.quickperf;

import java.util.Collection;

public class IssueThrower {

    public static final IssueThrower INSTANCE = new IssueThrower();

    private IssueThrower() {}

    public void throwIfNecessary( Throwable businessThrowable
                                , Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
    ) throws Throwable {

        if (onlyBusinessIssue(businessThrowable, groupOfPerfIssuesToFormat)) {
            throw businessThrowable;
        }

        if (onlyPerfIssues(businessThrowable, groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildPerfIssuesAssertionError(groupOfPerfIssuesToFormat);
        }

        if (businessIssue(businessThrowable) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildFunctionalIssueAndPerfIssuesAssertionError(businessThrowable
                                                                                 , groupOfPerfIssuesToFormat);
        }
    }

    private boolean onlyBusinessIssue(Throwable businessThrowable, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return businessIssue(businessThrowable) && !atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean businessIssue(Throwable businessThrowable) {
        return businessThrowable != null;
    }

    private boolean onlyPerfIssues(Throwable businessThrowable, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !businessIssue(businessThrowable) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean atLeastOnePerfIssue(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !groupOfPerfIssuesToFormat.isEmpty();
    }

}
