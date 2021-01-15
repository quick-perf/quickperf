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

package org.quickperf.issue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UnexpectedQuickPerfException extends RuntimeException {

    public UnexpectedQuickPerfException(Throwable cause) {
        super(buildMessage(cause), cause);
    }

    private static String buildMessage(Throwable cause) {

        String praise = "\uD83D\uDE4F";

        return System.lineSeparator()
                + System.lineSeparator()
                + "\t" + praise + " " + "Please help the QuickPerf OSS project by creating a pre-filled Github issue with this link"
                + System.lineSeparator()
                + "\t" + "https://github.com/quick-perf/quickperf/issues/new?"
                + "&labels=%3Abug%3A+bug&template=bug_report.md"
                + "&title=" + buildEncodedTitle(cause)
                + "&body=" + buildEncodedBody(cause);

    }

    private static String buildEncodedTitle(Throwable cause) {
        Throwable rootCause = searchRootCauseOf(cause);
        String title = "Unexpected exception: " + rootCause.getMessage();
        return encodeForUrl(title);
    }

    private static String buildEncodedBody(Throwable cause) {
        String stackTraceAsString = retrieveStackTraceAsStringFrom(cause);
        String body = "Stack trace" + System.lineSeparator() + stackTraceAsString;
        return encodeForUrl(body);
    }

    private static Throwable searchRootCauseOf(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static String encodeForUrl(String stackTraceAsString) {
        try {
            return URLEncoder.encode(stackTraceAsString, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            return "";
        }
    }

    private static String retrieveStackTraceAsStringFrom(Throwable cause) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        cause.printStackTrace(pw);
        return sw.toString();
    }

    public static boolean isQuickPerfException(Throwable exception) {
        if (hasQuickPerfPackage(exception)) {
            return true;
        }
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (hasQuickPerfPackage(cause)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasQuickPerfPackage(Throwable cause) {
        String packageName = cause.getClass().getPackage().getName();
        return packageName.contains("org.quickperf");
    }

}
