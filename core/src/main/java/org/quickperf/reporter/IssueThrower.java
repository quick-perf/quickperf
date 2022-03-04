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
package org.quickperf.reporter;

import org.quickperf.issue.JvmIssue;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.TestIssue;
import org.quickperf.issue.PerfIssuesToFormat;

import java.util.Collection;

class IssueThrower {

    static final IssueThrower INSTANCE = new IssueThrower();

    private IssueThrower() {}

    void throwIfNecessary( JvmOrTestIssue jvmOrTestIssue
                         , Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
    ) throws Throwable {

        if(jvmOrTestIssue.hasJvmIssue()) {
            JvmIssue jvmIssue = jvmOrTestIssue.getJvmIssue();
            throw jvmIssue.asThrowable();
        }

        TestIssue testIssue = jvmOrTestIssue.getTestIssue();

        if (onlyBusinessIssue(testIssue, groupOfPerfIssuesToFormat)) {
            throw testIssue.asThrowable();
        }

        if (onlyPerfIssues(testIssue, groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildPerfIssuesAssertionError(groupOfPerfIssuesToFormat);
        }

        if (businessIssue(testIssue) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat)) {
            throw ThrowableBuilder.buildFunctionalIssueAndPerfIssuesAssertionError(testIssue
                                                                                 , groupOfPerfIssuesToFormat);
        }

    }

    private boolean onlyBusinessIssue(TestIssue testIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return businessIssue(testIssue) && !atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean businessIssue(TestIssue testIssue) {
        return !testIssue.isNone();
    }

    private boolean onlyPerfIssues(TestIssue testIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !businessIssue(testIssue) && atLeastOnePerfIssue(groupOfPerfIssuesToFormat);
    }

    private boolean atLeastOnePerfIssue(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        return !groupOfPerfIssuesToFormat.isEmpty();
    }

}
