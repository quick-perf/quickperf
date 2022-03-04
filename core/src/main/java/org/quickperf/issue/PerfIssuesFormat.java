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
package org.quickperf.issue;

import java.util.Collection;

public interface PerfIssuesFormat {

    PerfIssuesFormat STANDARD = new PerfIssuesFormat() {

        @Override
        public String format(Collection<PerfIssue> perfIssues) {
            StringBuilder sb = new StringBuilder();
            int perfIssueCount = 0;
            for (PerfIssue perfIssue : perfIssues) {
                if (perfIssueCount != 0) {
                    sb.append(System.lineSeparator());
                    sb.append(System.lineSeparator());
                }
                perfIssueCount++;
                sb.append("[PERF] " + perfIssue.getDescription());
            }
            return sb.toString();
        }

    };

    String format(Collection<PerfIssue> perfIssues);

}
