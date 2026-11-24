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
import java.util.Arrays;
import java.util.List;

public class TestIssue implements Serializable {

    // Compared by class name, subclasses included, because core does not depend on the test frameworks
    private static final List<String> TEST_ABORT_EXCEPTIONS = Arrays.asList(
              "org.opentest4j.TestAbortedException"            // JUnit 5 assumption
            , "org.junit.internal.AssumptionViolatedException" // JUnit 4 assumption
            , "org.testng.SkipException"                       // TestNG skip
    );

    public static final TestIssue NONE = new TestIssue(null);

    private final Throwable throwable;

    // Kept when the throwable is replaced by a TestException because it cannot be serialized
    private final boolean abort;

    private final boolean thrownAfterTestMethod;

    private TestIssue(Throwable throwable) {
        this(throwable, isAbort(throwable), false);
    }

    private TestIssue(Throwable throwable, boolean abort, boolean thrownAfterTestMethod) {
        this.throwable = throwable;
        this.abort = abort;
        this.thrownAfterTestMethod = thrownAfterTestMethod;
    }

    public Throwable asThrowable() {
        return throwable;
    }

    /**
     * For a throwable thrown before the test method or by it, or when the test framework does not tell when it was
     * thrown.
     */
    public static TestIssue buildFrom(Throwable throwable) {
        return new TestIssue(throwable);
    }

    /**
     * For a throwable thrown once the test method has completed, by an <code>@After</code> or an
     * <code>@AfterEach</code> method for example. The performance recording of the test method is then complete.
     */
    public static TestIssue buildFromThrowableAfterTestMethod(Throwable throwable) {
        return new TestIssue(throwable, isAbort(throwable), true);
    }

    private static boolean isAbort(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        for (Class<?> type = throwable.getClass(); type != null; type = type.getSuperclass()) {
            if (TEST_ABORT_EXCEPTIONS.contains(type.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * For the throwables of a test that the test framework reported as failed in the new JVM. They are not an abort,
     * even if one of them, or a cause, is an abort exception: an assumption failing in the test method, with an
     * <code>@After</code> method failing afterwards, for example. A TestNG test skipped in the new JVM is reported
     * as failed there, with its <code>SkipException</code>, which then stays an abort.
     */
    public static TestIssue buildInNewJvmFrom(List<Throwable> throwables) {

        if (noThrowables(throwables)) {
            return TestIssue.NONE;
        }

        if(throwables.size() == 1) {
            Throwable throwable = throwables.get(0);
            if (isAbort(throwable)) {
                resetStackTraceOf(throwable);
                return new TestIssue(throwable, true, false);
            }
            Throwable cause = searchRootCauseOf(throwable);
            resetStackTraceOf(cause);
            return new TestIssue(cause, false, false);
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

        return new TestIssue(testException, testIssue.abort, testIssue.thrownAfterTestMethod);

    }

    private static TestIssue convertThrowablesIntoToBusinessOrTechnicalIssue(List<Throwable> throwables) {
        Throwable firstThrowable = throwables.get(0);

        for (int i = 2; i < throwables.size(); i++) {
            Throwable throwable = throwables.get(i);
            firstThrowable.addSuppressed(throwable);
        }

        return new TestIssue(firstThrowable, false, false);
    }

    public boolean isNone() {
        return throwable == null;
    }

    /**
     * @return true if the test framework aborted the test, for example because an assumption failed: an aborted
     * JUnit 5 test, a JUnit 4 assumption failure, or a skipped TestNG test
     */
    public boolean isAbort() {
        return abort;
    }

    /**
     * @return true if the throwable was thrown once the test method had completed
     * @see #buildFromThrowableAfterTestMethod(Throwable)
     */
    public boolean isThrownAfterTestMethod() {
        return thrownAfterTestMethod;
    }

}
