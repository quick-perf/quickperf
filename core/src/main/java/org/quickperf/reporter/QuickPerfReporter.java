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

package org.quickperf.reporter;

import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.TestExecutionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QuickPerfReporter {

    public static final QuickPerfReporter INSTANCE = new QuickPerfReporter();

    private final ConsoleReporter consoleReporter = ConsoleReporter.INSTANCE;

    private final IssueThrower issueThrower = IssueThrower.INSTANCE;

    private QuickPerfReporter() { }

    public void report(JvmOrTestIssue jvmOrTestIssue
                     , Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
                     , TestExecutionContext testExecutionContext) throws Throwable {

        if(testExecutionContext.areQuickPerfAnnotationsToBeDisplayed()) {
            consoleReporter.displayQuickPerfAnnotations(testExecutionContext.getPerfAnnotations());
        }

        if (testExecutionContext.isQuickPerfDebugMode()) {
            List<String> jvmOptions = testExecutionContext.getJvmOptions() == null ? new ArrayList<String>() : testExecutionContext.getJvmOptions().asStrings(testExecutionContext.getWorkingFolder());
            consoleReporter.displayQuickPerfDebugInfos(jvmOptions);
        }

        issueThrower.throwIfNecessary(jvmOrTestIssue, groupOfPerfIssuesToFormat);

    }

}
