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
package org.quickperf.junit4;

import org.junit.internal.AssumptionViolatedException;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.quickperf.issue.TestIssue;
import org.quickperf.testlauncher.TestRunnerFromMain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class JUnit4TestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    @Override
    public TestIssue executeTestMethod(Class<?> testClass, String methodName) {

        Request junitRequestOfMethod = Request.method(testClass, methodName);

        JUnitCore jUnitCore = new JUnitCore();
        AssumptionFailureRecorder assumptionFailureRecorder = new AssumptionFailureRecorder();
        jUnitCore.addListener(assumptionFailureRecorder);

        Result testResult = jUnitCore.run(junitRequestOfMethod);

        List<Failure> jUnit4Failures = testResult.getFailures();

        if (!jUnit4Failures.isEmpty()) {
            List<Throwable> jUnit4failuresAsThrowables = convertJUnit4FailuresToThrowables(jUnit4Failures);
            return TestIssue.buildInNewJvmFrom(jUnit4failuresAsThrowables);
        }

        // JUnit does not add the assumption failures to the failures of the result
        List<Throwable> assumptionFailures = assumptionFailureRecorder.assumptionFailures;
        if (!assumptionFailures.isEmpty()) {
            return buildTestAbortIssue(assumptionFailures.get(0));
        }

        return TestIssue.NONE;

    }

    private List<Throwable> convertJUnit4FailuresToThrowables(List<Failure> jUnit4Failures) {
        List<Throwable> throwables = new ArrayList<>();
        for (Failure failure : jUnit4Failures) {
            Throwable throwable = failure.getException();
            throwables.add(throwable);
        }
        return throwables;
    }

    private static TestIssue buildTestAbortIssue(Throwable assumptionFailure) {
        Throwable abort = toSerializableAbort(assumptionFailure);
        if (TestMethodCompletionInNewJvm.INSTANCE.isTestMethodCompleted()) {
            // An assumption failing in an @After method, for example: the performance recording of the test method is
            // complete. The test JVM evaluates it, as when it executes the test.
            return TestIssue.buildFromThrowableAfterTestMethod(abort);
        }
        // An assumption failing in a @Before method or in the test method, for example: the abort cut the performance
        // recording short, or the test method was not executed. The test JVM reports the abort without evaluating the
        // recording, as when it executes the test.
        return TestIssue.buildFrom(abort);
    }

    // TestIssueRepository would replace an abort that cannot be serialized with a failure. Before JUnit 4.13, an
    // assumption failing on a value, assumeTrue(boolean) or assumeThat(...) for example, keeps a matcher that cannot
    // be serialized.
    @SuppressWarnings("deprecation")
    private static Throwable toSerializableAbort(Throwable abort) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream())) {
            objectOutputStream.writeObject(abort);
            return abort;
        } catch (IOException notSerializable) {
            return new AssumptionViolatedException(abort.getMessage());
        }
    }

    private static class AssumptionFailureRecorder extends RunListener {

        private final List<Throwable> assumptionFailures = new CopyOnWriteArrayList<>();

        private volatile boolean testStarted;

        @Override
        public void testStarted(Description description) {
            testStarted = true;
        }

        // JUnit reports an assumption failing in a @BeforeClass or an @AfterClass method for the test class, not for
        // the test. In a @BeforeClass method, it aborts the test before the test starts. In an @AfterClass method, it
        // comes once the test has finished, and does not change the result of the test: the test JVM, which also
        // executes the @AfterClass methods, reports it for the class.
        @Override
        public void testAssumptionFailure(Failure failure) {
            if (failure.getDescription().isTest() || !testStarted) {
                assumptionFailures.add(failure.getException());
            }
        }

        // Before JUnit 4.12, JUnit reports the test class as ignored, without the assumption failure, when an
        // assumption fails in a @BeforeClass method
        @Override
        @SuppressWarnings("deprecation")
        public void testIgnored(Description description) {
            if (!description.isTest() && !testStarted) {
                String message = "An assumption failed before the test in the specific JVM where QuickPerf executes"
                               + " it, in a @BeforeClass method for example: JUnit ignored " + description;
                assumptionFailures.add(new AssumptionViolatedException(message));
            }
        }

    }

}
