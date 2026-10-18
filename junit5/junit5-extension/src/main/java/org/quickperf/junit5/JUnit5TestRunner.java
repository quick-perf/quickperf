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
package org.quickperf.junit5;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherFactory;
import org.opentest4j.TestAbortedException;
import org.quickperf.SystemProperties;
import org.quickperf.issue.JvmError;
import org.quickperf.issue.TestIssue;
import org.quickperf.testlauncher.TestRunnerFromMain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

class JUnit5TestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    private static final String JUPITER_ENGINE_SEGMENT = "[engine:junit-jupiter]";

    // Jupiter also aborts a test throwing this JUnit 4 exception, when JUnit 4 is on the classpath
    private static final String JUNIT4_ASSUMPTION_VIOLATED_EXCEPTION = "org.junit.internal.AssumptionViolatedException";

    @Override
    public TestIssue executeTestMethod(Class<?> testClass, String methodName) {
        String workingFolderPath = SystemProperties.WORKING_FOLDER.evaluate();
        TestToExecuteInNewJvm testToExecute = TestToExecuteInNewJvm.findIn(workingFolderPath);
        if (testToExecute == null) {
            String message = "QuickPerf did not find the JUnit unique id of " + testClass.getName() + "." + methodName
                           + " in the working folder " + workingFolderPath + ".";
            return TestIssue.buildFrom(JvmError.buildFrom(message));
        }

        String uniqueId = testToExecute.getUniqueId();
        TestExecutionRecorder testExecutionRecorder = new TestExecutionRecorder();
        try {
            runTest(uniqueId, testExecutionRecorder);
        } catch (Throwable unrecoverableError) {
            // JUnit rethrows the errors that it does not recover from, an OutOfMemoryError for example. Reporting them
            // lets this JVM end with System.exit, even if the test left non-daemon threads.
            return TestIssue.buildFrom(buildUnrecoverableError(uniqueId, unrecoverableError));
        }

        if (testExecutionRecorder.startedTestCount.get() != 1) {
            if (testExecutionRecorder.onlyContainersAborted()) {
                // An assumption failing in a @BeforeAll method or in an arguments source, for example
                Throwable containerAbort = testExecutionRecorder.containerAborts.get(0);
                return TestIssue.buildFrom(new TestAbortedBeforeTestMethodInNewJvm(toSerializableAbort(containerAbort)));
            }
            JvmError jvmError = buildNotOneStartedTestError(uniqueId, testExecutionRecorder);
            return TestIssue.buildFrom(jvmError);
        }

        List<Throwable> failures = testExecutionRecorder.failures;
        if (!failures.isEmpty()) {
            return TestIssue.buildInNewJvmFrom(failures);
        }

        List<Throwable> testAborts = testExecutionRecorder.testAborts;
        if (!testAborts.isEmpty()) {
            return buildTestAbortIssue(testAborts.get(0));
        }

        return TestIssue.NONE;
    }

    private static void runTest(String uniqueId, TestExecutionListener listener) {
        LauncherDiscoveryRequest request = request().selectors(selectUniqueId(removeSuiteSegments(uniqueId)))
                                          .build();

        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);
    }

    // The unique id of a test executed through a suite (@Suite) starts with segments of the suite engine. The new JVM
    // executes the test with the Jupiter engine only: the @BeforeSuite and @AfterSuite methods are not executed again.
    private static String removeSuiteSegments(String uniqueId) {
        int jupiterEngineSegmentIndex = uniqueId.lastIndexOf(JUPITER_ENGINE_SEGMENT);
        return jupiterEngineSegmentIndex == -1 ? uniqueId : uniqueId.substring(jupiterEngineSegmentIndex);
    }

    private static TestIssue buildTestAbortIssue(Throwable testAbort) {
        TestMethodInvocationInNewJvm testMethodInvocation = TestMethodInvocationInNewJvm.INSTANCE;
        if (!testMethodInvocation.isTestMethodInvoked()) {
            // An assumption failing in a @BeforeEach method, for example
            return TestIssue.buildFrom(new TestAbortedBeforeTestMethodInNewJvm(toSerializableAbort(testAbort)));
        }

        Throwable testMethodThrowable = testMethodInvocation.getTestMethodThrowable();
        if (testMethodThrowable != null && !isAbort(testMethodThrowable) && isACauseOf(testAbort, testMethodThrowable)) {
            // A retry extension aborted the failed test to execute it again. The test JVM gets the failure, as when it
            // executes the test, so that the retry extension handles it there.
            return TestIssue.buildInNewJvmFrom(Collections.singletonList(testMethodThrowable));
        }

        // An assumption failing in the test method or in an @AfterEach method, for example. The test JVM evaluates the
        // performance records, as when it executes the test.
        return TestIssue.buildFrom(toSerializableAbort(testAbort));
    }

    private static boolean isAbort(Throwable throwable) {
        if (throwable instanceof TestAbortedException) {
            return true;
        }
        for (Class<?> type = throwable.getClass(); type != null; type = type.getSuperclass()) {
            if (type.getName().equals(JUNIT4_ASSUMPTION_VIOLATED_EXCEPTION)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isACauseOf(Throwable throwable, Throwable possibleCause) {
        Set<Throwable> causes = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        for (Throwable cause = throwable.getCause(); cause != null && causes.add(cause); cause = cause.getCause()) {
            if (cause == possibleCause) {
                return true;
            }
        }
        return false;
    }

    // TestIssueRepository would replace an abort that cannot be serialized with a failure
    private static Throwable toSerializableAbort(Throwable abort) {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new ByteArrayOutputStream())) {
            objectOutputStream.writeObject(abort);
            return abort;
        } catch (IOException notSerializable) {
            return new TestAbortedException(abort.getMessage());
        }
    }

    private static JvmError buildUnrecoverableError(String uniqueId, Throwable unrecoverableError) {
        StringWriter stackTrace = new StringWriter();
        unrecoverableError.printStackTrace(new PrintWriter(stackTrace));
        String message = "JUnit stopped in the specific JVM where QuickPerf executes " + uniqueId + ":"
                       + System.lineSeparator() + stackTrace;
        return JvmError.buildFrom(message);
    }

    private static JvmError buildNotOneStartedTestError(String uniqueId, TestExecutionRecorder testExecutionRecorder) {
        String lineSeparator = System.lineSeparator();
        StringBuilder message = new StringBuilder();
        message.append("JUnit started ").append(testExecutionRecorder.startedTestCount.get())
               .append(" tests instead of 1 in the specific JVM where QuickPerf executes ").append(uniqueId).append('.')
               .append(lineSeparator)
               .append("QuickPerf executes a test in a specific JVM for annotations such as @HeapSize, @JvmOptions,")
               .append(" @MeasureHeapAllocation or @ProfileJvm.");
        List<String> unsuccessfulExecutions = testExecutionRecorder.unsuccessfulExecutions;
        if (!unsuccessfulExecutions.isEmpty()) {
            message.append(lineSeparator).append("Reported by JUnit in the specific JVM:");
            for (String unsuccessfulExecution : unsuccessfulExecutions) {
                message.append(lineSeparator).append("- ").append(unsuccessfulExecution);
            }
        }
        message.append(lineSeparator).append("Possible causes:")
               .append(lineSeparator)
               .append("- A condition (@EnabledIfSystemProperty, @DisabledIf, an ExecutionCondition extension...)")
               .append(" disables the test or its class in the specific JVM. The specific JVM does not get the system")
               .append(" properties of the test JVM: you can set them with @JvmOptions(\"-Dname=value\").");
        if (isTemplateInvocation(uniqueId)) {
            message.append(lineSeparator)
                   .append("- The arguments source of a parameterized test provides other arguments in the specific")
                   .append(" JVM. It has to provide the same arguments, in the same order, in every JVM: do not use")
                   .append(" Set.of(...), a HashSet or random values.")
                   .append(lineSeparator)
                   .append("- The invocations of a test template depend on the results of the previous invocations, as")
                   .append(" with retry extensions such as junit-pioneer @RetryingTest: only the first attempt exists in")
                   .append(" the specific JVM.");
        }
        message.append(lineSeparator)
               .append("- A failure before the test, for example in a @BeforeAll method.");
        return JvmError.buildFrom(message.toString());
    }

    private static boolean isTemplateInvocation(String uniqueId) {
        return uniqueId.contains("[test-template-invocation:") || uniqueId.contains("[class-template-invocation:");
    }

    private static class TestExecutionRecorder implements TestExecutionListener {

        private final AtomicInteger startedTestCount = new AtomicInteger();

        private final AtomicInteger skippedCount = new AtomicInteger();

        private final List<Throwable> failures = new CopyOnWriteArrayList<>();

        private final List<Throwable> testAborts = new CopyOnWriteArrayList<>();

        private final List<Throwable> containerAborts = new CopyOnWriteArrayList<>();

        private final List<String> unsuccessfulExecutions = new CopyOnWriteArrayList<>();

        @Override
        public void executionSkipped(TestIdentifier testIdentifier, String reason) {
            skippedCount.incrementAndGet();
            unsuccessfulExecutions.add(testIdentifier.getDisplayName() + " skipped: " + reason);
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            if (testIdentifier.isTest()) {
                startedTestCount.incrementAndGet();
            }
        }

        @Override
        public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
            if (!testExecutionResult.getThrowable().isPresent()) {
                return;
            }
            Throwable throwable = testExecutionResult.getThrowable().get();
            if (testExecutionResult.getStatus() == FAILED) {
                failures.add(throwable);
                unsuccessfulExecutions.add(testIdentifier.getDisplayName() + " failed: " + rootCauseOf(throwable));
            } else {
                if (testIdentifier.isTest()) {
                    testAborts.add(throwable);
                } else {
                    containerAborts.add(throwable);
                }
                unsuccessfulExecutions.add(testIdentifier.getDisplayName() + " aborted: " + throwable);
            }
        }

        private boolean onlyContainersAborted() {
            return startedTestCount.get() == 0 && skippedCount.get() == 0 && failures.isEmpty()
                && !containerAborts.isEmpty();
        }

        private static Throwable rootCauseOf(Throwable throwable) {
            Set<Throwable> causes = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
            Throwable rootCause = throwable;
            while (rootCause.getCause() != null && causes.add(rootCause)) {
                rootCause = rootCause.getCause();
            }
            return rootCause;
        }

    }

}
