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

package org.quickperf.sql;

import org.quickperf.issue.PerfIssue;
import org.quickperf.unit.Count;

public class SqlStatementPerfIssueBuilder {

    private SqlStatementPerfIssueBuilder() {}

    public static SqlStatementPerfIssueBuilder aSqlPerfIssue() {
        return new SqlStatementPerfIssueBuilder();
    }

    public PerfIssue buildNotEqualNumberOfStatements(Count measuredCount, Count expectedCount, String requestType) {
        String assertionMessage = "Expected number of " + requestType + " statements "
                               + "<" + expectedCount.getValue() + ">"
                               + " but is " + "<" + measuredCount.getValue() + ">" + ".";
        return new PerfIssue(assertionMessage);
    }

}
