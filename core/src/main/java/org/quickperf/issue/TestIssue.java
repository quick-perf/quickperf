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

import java.io.Serializable;
import java.util.List;

public class TestIssue implements Serializable {

    public static final TestIssue NONE = new TestIssue(null);

    private final Throwable throwable;

    private TestIssue(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable asThrowable() {
        return throwable;
    }

    public static TestIssue buildFrom(Throwable throwable) {
        return new TestIssue(throwable);
    }

    public static TestIssue buildInNewJvmFrom(List<Throwable> throwables) {

        if (noThrowables(throwables)) {
            return TestIssue.NONE;
        }

        if(throwables.size() == 1) {
            Throwable throwable = throwables.get(0);
            Throwable cause = searchRootCauseOf(throwable);
            resetStackTraceOf(cause);
            return new TestIssue(cause);
        }

        return convertThrowablesIntoToBusinessOrTechnicalIssue(throwables);

    }

    private static boolean noThrowables(List<Throwable> throwables) {
        return throwables == null || throwables.isEmpty();
    }

    private static Throwable searchRootCauseOf(Throwable throwable) {
        Throwable cause = throwable;
        while(cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static void resetStackTraceOf(Throwable cause) {
        cause.setStackTrace(new StackTraceElement[0]);
    }

    public static TestIssue buildSerializableTestIssueFrom(TestIssue testIssue) {

        Throwable nonSerializableThrowable = testIssue.asThrowable();

        Throwable cause = searchRootCauseOf(nonSerializableThrowable);

        String causeMessage = cause.getMessage();
        String className = nonSerializableThrowable.getClass().getCanonicalName();

        String message = className + ": " + causeMessage;

        TestException testException = TestException.buildFrom(message);

        return new TestIssue(testException);

    }

    private static TestIssue convertThrowablesIntoToBusinessOrTechnicalIssue(List<Throwable> throwables) {
        Throwable firstThrowable = throwables.get(0);

        for (int i = 2; i < throwables.size(); i++) {
            Throwable throwable = throwables.get(i);
            firstThrowable.addSuppressed(throwable);
        }

        return new TestIssue(firstThrowable);
    }

    public boolean isNone() {
        return throwable == null;
    }

}
