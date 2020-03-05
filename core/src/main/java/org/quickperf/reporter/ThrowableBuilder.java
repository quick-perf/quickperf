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

import org.quickperf.issue.BusinessOrTechnicalIssue;
import org.quickperf.issue.PerfIssuesToFormat;

import java.util.Collection;

class ThrowableBuilder {

    private ThrowableBuilder() { }

    static AssertionError buildPerfIssuesAssertionError(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        String perfIssuesDescription = numberOfPerfIssuesAsString(groupOfPerfIssuesToFormat)
                                     + convertPerfIssuesToString(groupOfPerfIssuesToFormat);
        AssertionError assertionError = new AssertionError(perfIssuesDescription);
        StackTraceElement[] emptyStack = new StackTraceElement[0];
        assertionError.setStackTrace(emptyStack);
        return assertionError;
    }

    private static String numberOfPerfIssuesAsString(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {

        int numberOfPerfIssues = 0;
        for (PerfIssuesToFormat perfIssuesToFormat : groupOfPerfIssuesToFormat) {
            numberOfPerfIssues += perfIssuesToFormat.getNumberOfPerfIssues();
        }

        if (numberOfPerfIssues == 1) {
            return "a performance property is not respected";
        }

        return numberOfPerfIssues + " performance properties are not respected";

    }

    private static String convertPerfIssuesToString(Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {
        StringBuilder sb = new StringBuilder();
        for (PerfIssuesToFormat perfIssuesToFormat : groupOfPerfIssuesToFormat) {
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
            sb.append(perfIssuesToFormat.format());
        }
        return sb.toString();
    }

    static AssertionError buildFunctionalIssueAndPerfIssuesAssertionError(BusinessOrTechnicalIssue businessOrTechnicalIssue, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {

        Throwable throwable = businessOrTechnicalIssue.getThrowable();

        String message = buildBusinessIssueAndPerfIssuesMessage(throwable, groupOfPerfIssuesToFormat);

        AssertionError assertionError = new AssertionError(message, throwable.getCause());

        assertionError.setStackTrace(throwable.getStackTrace());

        return assertionError;

    }

    private static String buildBusinessIssueAndPerfIssuesMessage(Throwable throwable, Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat) {

        StringBuilder sb = new StringBuilder();

        sb.append("Performance and functional properties not respected");

        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("PERFORMANCE PROPERTIES(S)");
        String perfIssuesDescription = convertPerfIssuesToString(groupOfPerfIssuesToFormat);
        sb.append(perfIssuesDescription);

        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append("FUNCTIONAL PROPERTY");
        sb.append(System.lineSeparator());
        String businessMessage = throwable.getMessage();
        sb.append(businessMessage);

        return sb.toString();

    }

}
