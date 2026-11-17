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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

class JUnit5TestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    private static final String JUPITER_ENGINE_SEGMENT = "[engine:junit-jupiter]";

    private static final String TEST_FACTORY_SEGMENT = "[test-factory:";

    private static final String DYNAMIC_TEST_SEGMENT = "[dynamic-test:";

    private static final String PARALLEL_EXECUTION_ENABLED = "junit.jupiter.execution.parallel.enabled";

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

        // The unique id that this JVM executes, given by all the messages
        String uniqueId = removeSuiteSegments(testToExecute.getUniqueId());
        TestExecutionRecorder testExecutionRecorder = new TestExecutionRecorder(uniqueId);
        try {
            runTest(uniqueId, testToExecute.getDisplayNameConfigurationParameters(), testExecutionRecorder);
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

        JvmError otherInvocationError = findOtherInvocationError(uniqueId, testToExecute, testExecutionRecorder);
        if (otherInvocationError != null) {
            // This JVM may have measured another invocation than the one reported by the test JVM
            return TestIssue.buildFrom(otherInvocationError);
        }

        String executedDisplayName = testExecutionRecorder.startedTestDisplayName;
        if (isDynamicTest(uniqueId) && !testToExecute.getDisplayName().equals(executedDisplayName)) {
            // This JVM may have measured another dynamic test than the one reported by the test JVM
            JvmError jvmError = buildOtherDisplayNameError(uniqueId, testToExecute.getDisplayName(), executedDisplayName);
            return TestIssue.buildFrom(jvmError);
        }

        List<Throwable> testFailures = testExecutionRecorder.testFailures;
        if (!testFailures.isEmpty()) {
            return TestIssue.buildInNewJvmFrom(testFailures);
        }

        List<Throwable> testAborts = testExecutionRecorder.testAborts;
        if (!testAborts.isEmpty()) {
            return buildTestAbortIssue(testAborts.get(0));
        }

        return TestIssue.NONE;
    }

    // This JVM executes one test. The parallel execution can still be enabled here, by a junit-platform.properties file
    // of the class path or by a system property. In the concurrent mode (junit.jupiter.execution.parallel.mode.default or
    // @Execution), JUnit would then execute a dynamic test, or a test template invocation, in another thread while the
    // test factory method, or the arguments source, goes on providing the next ones, and QuickPerf would also measure
    // what they do meanwhile. A configuration parameter of the request takes precedence over both.
    private static void runTest(String uniqueId, Map<String, String> displayNameConfigurationParameters
                              , TestExecutionListener listener) {
        LauncherDiscoveryRequest request = request().selectors(selectUniqueId(uniqueId))
                                          .configurationParameters(displayNameConfigurationParameters)
                                          .configurationParameter(PARALLEL_EXECUTION_ENABLED, "false")
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
                   .append(" Set.of(...), a HashSet or random values. If the arguments come from the methods of a")
                   .append(" class, sort these methods, for example by Method.toString(): Class.getDeclaredMethods()")
                   .append(" and Class.getMethods() return them in an order that changes from one JVM to another.")
                   .append(lineSeparator)
                   .append("- The invocations of a test template depend on the results of the previous invocations, as")
                   .append(" with retry extensions such as junit-pioneer @RetryingTest: only the first attempt exists in")
                   .append(" the specific JVM.");
        }
        if (isDynamicTest(uniqueId)) {
            message.append(lineSeparator)
                   .append("- The test factory method provides other dynamic tests in the specific JVM. It has to provide")
                   .append(" the same dynamic tests and dynamic containers, in the same order, in every JVM: do not use")
                   .append(" Set.of(...), Map.of(...), a HashSet or random values. If the dynamic tests come from the")
                   .append(" methods of a class, sort these methods, for example by Method.toString():")
                   .append(" Class.getDeclaredMethods() and Class.getMethods() return them in an order that changes")
                   .append(" from one JVM to another.")
                   .append(lineSeparator)
                   .append("- A failure before the dynamic test, for example in a @BeforeAll method, in a @BeforeEach")
                   .append(" method or in the test factory method. The test JVM also executes the @BeforeEach methods of")
                   .append(" the test factory: it holds what they acquire, a lock, a port or a file for example, until its")
                   .append(" @AfterEach methods, while the specific JVM executes the dynamic test.");
        } else {
            message.append(lineSeparator)
                   .append("- A failure before the test, for example in a @BeforeAll method.");
        }
        return JvmError.buildFrom(message.toString());
    }

    private static boolean isTemplateInvocation(String uniqueId) {
        return uniqueId.contains("[test-template-invocation:") || uniqueId.contains("[class-template-invocation:");
    }

    private static boolean isDynamicTest(String uniqueId) {
        return uniqueId.contains(DYNAMIC_TEST_SEGMENT);
    }

    // The test JVM saved the display names of the invocations of parameterized tests and parameterized classes of the
    // test and of its ancestors, from the outermost one: null if this JVM executed them with the same display names
    private static JvmError findOtherInvocationError(String uniqueId, TestToExecuteInNewJvm testToExecute
                                                   , TestExecutionRecorder testExecutionRecorder) {
        for (Map.Entry<String, String> invocation : testToExecute.getParameterizedInvocationDisplayNames().entrySet()) {
            String invocationUniqueId = removeSuiteSegments(invocation.getKey());
            String displayName = invocation.getValue();
            String executedDisplayName = testExecutionRecorder.displayNameByUniqueId.get(invocationUniqueId);
            if (executedDisplayName != null && !executedDisplayName.equals(displayName)) {
                boolean classInvocation = !invocationUniqueId.equals(uniqueId);
                return buildOtherInvocationError(uniqueId, classInvocation, displayName, executedDisplayName
                                               , testToExecute.getDisplayName());
            }
        }
        return null;
    }

    // When the arguments source provides its arguments in another order in this JVM, JUnit executes another invocation
    // than the one of the test JVM. QuickPerf cannot tell it from a display name changing from one JVM to another.
    private static JvmError buildOtherInvocationError(String uniqueId, boolean classInvocation, String displayName
                                                    , String executedDisplayName, String testDisplayName) {
        String lineSeparator = System.lineSeparator();
        StringBuilder message = new StringBuilder();
        message.append("In the specific JVM where QuickPerf executes ").append(uniqueId)
               .append(", the display name of the ").append(classInvocation ? "class invocation" : "invocation")
               .append(" is \"").append(executedDisplayName)
               .append("\", and not \"").append(displayName).append("\": QuickPerf cannot report the measures and")
               .append(" the failures of this JVM for \"").append(testDisplayName).append("\".")
               .append(lineSeparator).append("Possible causes:")
               .append(lineSeparator)
               .append("- The arguments source provides other arguments, or the same arguments in another order, in")
               .append(" the specific JVM. It has to provide the same arguments, in the same order, in every JVM: do")
               .append(" not use Set.of(...), Map.of(...), a HashSet or random values. If the arguments come from the")
               .append(" methods of a class, sort these methods, for example by Method.toString():")
               .append(" Class.getDeclaredMethods() and Class.getMethods() return them in an order that changes from")
               .append(" one JVM to another.")
               .append(lineSeparator)
               .append("- The display name of the invocation changes from one JVM to another, for example because an")
               .append(" argument has an Object.toString() that is not overridden, or contains a time, a random value")
               .append(" or a @TempDir path. Give the invocation a display name that does not change, for example with")
               .append(" @ParameterizedTest(name = \"...\"), @ParameterizedClass(name = \"...\") or")
               .append(" Named.of(\"...\", argument).")
               .append(lineSeparator)
               .append("- The display name of the invocation depends on a system property of the test JVM,")
               .append(" user.language or user.timezone for example: the specific JVM does not get the system")
               .append(" properties of the test JVM. Give the invocation a display name that does not depend on them,")
               .append(" or set the same system property with @JvmOptions(\"-Dname=value\").");
        return JvmError.buildFrom(message.toString());
    }

    // When the test factory method provides its dynamic tests in another order in this JVM, JUnit executes another dynamic
    // test than the one of the test JVM. QuickPerf cannot tell it from a display name changing from one JVM to another.
    private static JvmError buildOtherDisplayNameError(String uniqueId, String displayName, String executedDisplayName) {
        String lineSeparator = System.lineSeparator();
        StringBuilder message = new StringBuilder();
        message.append("In the specific JVM where QuickPerf executes ").append(uniqueId)
               .append(", the display name of the dynamic test is \"").append(executedDisplayName)
               .append("\", and not \"").append(displayName).append("\": QuickPerf cannot report the measures and")
               .append(" the failures of this JVM for \"").append(displayName).append("\".")
               .append(lineSeparator).append("Possible causes:")
               .append(lineSeparator)
               .append("- The test factory method provides other dynamic tests, or the same ones in another order, in")
               .append(" the specific JVM. It has to provide the same dynamic tests and dynamic containers, in the same")
               .append(" order, in every JVM: do not use Set.of(...), Map.of(...), a HashSet or random values. If the")
               .append(" dynamic tests come from the methods of a class, sort these methods, for example by")
               .append(" Method.toString(): Class.getDeclaredMethods() and Class.getMethods() return them in an order")
               .append(" that changes from one JVM to another.")
               .append(lineSeparator)
               .append("- The display name of the dynamic test changes from one JVM to another, for example because it")
               .append(" contains an Object.toString() that is not overridden, a time, a random value or a @TempDir")
               .append(" path. Give the dynamic test a display name that does not change.")
               .append(lineSeparator)
               .append("- The display name of the dynamic test depends on a system property of the test JVM,")
               .append(" user.language or user.timezone for example: the specific JVM does not get the system")
               .append(" properties of the test JVM. Give the dynamic test a display name that does not depend on them,")
               .append(" for example with String.format(Locale.ROOT, ...), or set the same system property with")
               .append(" @JvmOptions(\"-Dname=value\").");
        return JvmError.buildFrom(message.toString());
    }

    // For example [engine:junit-jupiter]/[class:...]/[test-factory:dynamic_tests()] for the dynamic test
    // [engine:junit-jupiter]/[class:...]/[test-factory:dynamic_tests()]/[dynamic-container:#1]/[dynamic-test:#2]
    private static String findTestFactoryUniqueId(String dynamicTestUniqueId) {
        int testFactorySegmentIndex = dynamicTestUniqueId.indexOf(TEST_FACTORY_SEGMENT);
        int testFactorySegmentEndIndex = dynamicTestUniqueId.indexOf(']', testFactorySegmentIndex);
        return dynamicTestUniqueId.substring(0, testFactorySegmentEndIndex + 1);
    }

    private static class TestExecutionRecorder implements TestExecutionListener {

        private final String uniqueId;

        // null if the test is not a dynamic test
        private final String testFactoryUniqueId;

        private final AtomicInteger startedTestCount = new AtomicInteger();

        private volatile String startedTestDisplayName;

        private final AtomicInteger skippedCount = new AtomicInteger();

        private final List<Throwable> failures = new CopyOnWriteArrayList<>();

        // The test JVM executes the test factory method of a dynamic test, with its @BeforeEach and @AfterEach methods,
        // but not the dynamic test. This JVM executes them with this dynamic test only. A failure of the test factory, or
        // of a dynamic container, after the dynamic test may only happen here, so it fails the dynamic test. It can come
        // from the dynamic test, for example an @AfterEach method checking what it did, or from the other dynamic tests,
        // not executed here, for example a check that all the Mockito stubs were used.
        private final List<Throwable> testFailures = new CopyOnWriteArrayList<>();

        private final List<Throwable> testAborts = new CopyOnWriteArrayList<>();

        private final List<Throwable> containerAborts = new CopyOnWriteArrayList<>();

        private final List<String> unsuccessfulExecutions = new CopyOnWriteArrayList<>();

        // The tests and the containers that JUnit started
        private final Map<String, String> displayNameByUniqueId = new ConcurrentHashMap<>();

        private TestExecutionRecorder(String uniqueId) {
            this.uniqueId = uniqueId;
            this.testFactoryUniqueId = isDynamicTest(uniqueId) ? findTestFactoryUniqueId(uniqueId) : null;
        }

        @Override
        public void executionSkipped(TestIdentifier testIdentifier, String reason) {
            skippedCount.incrementAndGet();
            unsuccessfulExecutions.add(testIdentifier.getDisplayName() + " skipped: " + reason);
        }

        @Override
        public void executionStarted(TestIdentifier testIdentifier) {
            displayNameByUniqueId.put(testIdentifier.getUniqueId(), testIdentifier.getDisplayName());
            if (testIdentifier.isTest()) {
                startedTestCount.incrementAndGet();
                startedTestDisplayName = testIdentifier.getDisplayName();
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
                testFailures.add(isTestFactoryOrDynamicContainer(testIdentifier)
                                 ? buildFailureAfterTheDynamicTest(testIdentifier, throwable)
                                 : throwable);
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

        private boolean isTestFactoryOrDynamicContainer(TestIdentifier testIdentifier) {
            return testFactoryUniqueId != null
                && testIdentifier.isContainer()
                && testIdentifier.getUniqueId().startsWith(testFactoryUniqueId);
        }

        // The test JVM keeps the root cause of a failure only: the message gives the failure
        private AssertionError buildFailureAfterTheDynamicTest(TestIdentifier testIdentifier, Throwable throwable) {
            String message = "JUnit reported a failure of " + testIdentifier.getDisplayName() + " after the dynamic test"
                           + " in the specific JVM where QuickPerf executes " + uniqueId + ": " + rootCauseOf(throwable);
            return new AssertionError(message);
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
