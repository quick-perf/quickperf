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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.TestIssue;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.jvm.JVM;
import org.quickperf.perfrecording.PerformanceRecording;
import org.quickperf.reporter.QuickPerfReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(QuickPerfTestExtension.class);

    private final QuickPerfConfigs quickPerfConfigs =  QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        int junit5AllocationOffset = findJunit5AllocationOffset();
        TestExecutionContext testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , extensionContext.getRequiredTestMethod()
                                                            , junit5AllocationOffset);
        extensionContext.getStore(NAMESPACE).put(TestExecutionContext.class, testExecutionContext);
    }

    private TestExecutionContext getTestExecutionContext(ExtensionContext extensionContext) {
        return extensionContext.getStore(NAMESPACE).get(TestExecutionContext.class, TestExecutionContext.class);
    }

    private int findJunit5AllocationOffset() {
        JVM.Version jvmVersion = JVM.INSTANCE.version;
        if(jvmVersion.isGreaterThanOrEqualTo16()) {
            return  48;
        }
        return 40;
    }

    // we need to skip BeforeEach/AfterEach if we plan to fork as they will be executed in the forked VM
    // FIXME we should also do this for BeforeAll/AfterAll but the TestExecutionContext is not yet created so we don't know at this stage that we need to fork.
    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(invocationContext.getTargetClass().equals(QuickPerfTestExtension.class)){
            // We proceed with our own BeforeEach as we need it for all cases.
            // Note that currently this never happens as our BeforeEach is not intercepted by our own extension,
            // maybe it's a bug in JUnit or maybe not but we keep this as a safeguard.
            invocation.proceed();
        }
        else if (getTestExecutionContext(extensionContext).testExecutionUsesTwoJVMs() && !SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // we skip the BeforeEach if the test will fork
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (getTestExecutionContext(extensionContext).testExecutionUsesTwoJVMs() && !SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // we skip the AfterEach if the test will fork
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        // Be careful that this method will be called by each invocation of the test template defines by a single test template method.
        // Normal lifecycle will apply.
        // There is no allocation offset with template method
        TestExecutionContext testExecutionContext = getTestExecutionContext(extensionContext);
        testExecutionContext.setRunnerAllocationOffset(0);

        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeTestMethodInNewJvmAndRecordPerformance(invocation, invocationContext, testExecutionContext);
            return;
        }

        JvmOrTestIssue jvmOrTestIssue = executeTestMethodAndRecordPerformance(invocation, invocationContext, testExecutionContext);
        processJvmOrTestIssue(jvmOrTestIssue, testExecutionContext);
    }

    @Override
    public void interceptTestMethod(  Invocation<Void> invocation
                                    , ReflectiveInvocationContext<Method> invocationContext
                                    , ExtensionContext extensionContext) throws Throwable {
        TestExecutionContext testExecutionContext = getTestExecutionContext(extensionContext);
        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeTestMethodInNewJvmAndRecordPerformance(invocation, invocationContext, testExecutionContext);
            return;
        }

        JvmOrTestIssue jvmOrTestIssue = executeTestMethodAndRecordPerformance(invocation, invocationContext, testExecutionContext);
        processJvmOrTestIssue(jvmOrTestIssue, testExecutionContext);
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        // Be careful that this method will be called by each dynamic tests defines by a single test factory method.
        // And that @BeforeEach and @AfterEach methods will be invoked onces for all dynamic test produced by a test factory method.
        // This means that we will use the same TestExecutionContext for all dynamic tests produced by a test factory method.
        // So the annotation from the test factory method will be used on all dynamic tests produced by it.

        TestExecutionContext testExecutionContext = getTestExecutionContext(extensionContext);
        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            // FIXME unable to fork the VM for dynamic test, see https://github.com/junit-team/junit5/issues/2399
            throw new RuntimeException("Cannot run a dynamic test on a forked JVM");
        }

        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            // FIXME unable to fork the VM for dynamic test, see https://github.com/junit-team/junit5/issues/2399
            throw new RuntimeException("Cannot run a dynamic test on a forked JVM");
        }

        TestIssue testIssue = executeTestMethodAndRecordPerformanceInSameJvm(invocation, testExecutionContext);
        JvmOrTestIssue jvmOrTestIssue = JvmOrTestIssue.buildFrom(testIssue);
        processJvmOrTestIssue(jvmOrTestIssue, testExecutionContext);
    }

    private void processJvmOrTestIssue(JvmOrTestIssue jvmOrTestIssue, TestExecutionContext testExecutionContext) throws Throwable {
        SetOfAnnotationConfigs testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
                = perfIssuesEvaluator.evaluatePerfIssuesIfNoJvmIssue(testAnnotationConfigs
                , testExecutionContext
                , jvmOrTestIssue);
        testExecutionContext.cleanResources();

        quickPerfReporter.report(jvmOrTestIssue
                               , groupOfPerfIssuesToFormat
                               , testExecutionContext);
    }

    private void executeTestMethodInNewJvmAndRecordPerformance(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, TestExecutionContext testExecutionContext) throws IllegalAccessException, InvocationTargetException {
        Object[] args = invocationContext.getArguments().toArray();
        Object target = invocationContext.getTarget().orElse(null);
        Method method = makeAccessible(invocationContext.getExecutable());
        invocation.skip();//skip the invocation as we directly invoke the test method

        performanceRecording.start(testExecutionContext);

        try {
            //directly invoke the method to lower the interaction between JUnit, other extensions and QuickPerf.
            method.invoke(target, args);
        } finally {
            performanceRecording.stop(testExecutionContext);
        }
    }

    @SuppressWarnings("deprecation")
    private Method makeAccessible(Method executable) {
        if(!executable.isAccessible()){
            executable.setAccessible(true);
        }
        return executable;
    }

    private JvmOrTestIssue executeTestMethodAndRecordPerformance(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, TestExecutionContext testExecutionContext) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            Method testMethod = invocationContext.getExecutable();
            Class<?> testClass = invocationContext.getTargetClass();
            JvmOrTestIssue jvmOrTestIssue = executeTestMethodInNewJvm(testClass, testMethod, testExecutionContext);
            tryToSkipInvocation(invocation); // because the test method is invoked directly inside the 'newJvmTestLauncher'
            return jvmOrTestIssue;
        }
        TestIssue testIssue = executeTestMethodAndRecordPerformanceInSameJvm(invocation, testExecutionContext);
        return JvmOrTestIssue.buildFrom(testIssue);
    }

    private void tryToSkipInvocation(Invocation<Void> invocation) {
        try {
            invocation.skip();
        } catch(NoSuchMethodError noSuchMethodError) {
            System.err.println("[QUICK PERF] A JUnit 5 version equal to or greater than 5.6.0 is required.");
            throw noSuchMethodError;
        }
    }

    private JvmOrTestIssue executeTestMethodInNewJvm(Class<?> testClass, Method testMethod, TestExecutionContext testExecutionContext) {
        NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
        return newJvmTestLauncher.executeTestMethodInNewJvm(testClass
                                                          , testMethod
                                                          , testExecutionContext
                                                          , QuickPerfJunit5Core.class);
    }

    private TestIssue executeTestMethodAndRecordPerformanceInSameJvm(Invocation<Void> invocation, TestExecutionContext testExecutionContext) {
        performanceRecording.start(testExecutionContext);
        try {
            invocation.proceed();
            return TestIssue.NONE;
        } catch (Throwable throwable) {
            return TestIssue.buildFrom(throwable);
        } finally {
            performanceRecording.stop(testExecutionContext);
        }
    }

}
