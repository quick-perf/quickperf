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

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.JvmError;
import org.quickperf.issue.JvmIssue;
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
import java.util.Collections;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(QuickPerfTestExtension.class);

    // A dynamic test is not invoked by reflection: there is no allocation to subtract
    private static final int DYNAMIC_TEST_ALLOCATION_OFFSET = 0;

    private final QuickPerfConfigs quickPerfConfigs =  QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        Method testMethod = extensionContext.getRequiredTestMethod();
        if (isTestFactory(testMethod)) {
            // A test factory has no TestExecutionContext: each of its dynamic tests has its own, see
            // executeDynamicTestAndRecordPerformance. QuickPerf measures neither the test factory method nor its
            // @BeforeEach and @AfterEach methods.
            return;
        }
        int junit5AllocationOffset = findJunit5AllocationOffset();
        TestExecutionContext testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , testMethod
                                                            , junit5AllocationOffset);
        extensionContext.getStore(NAMESPACE).put(TestExecutionContext.class, testExecutionContext);
    }

    // As JUnit, which executes a method returning void, and annotated with @TestFactory and @Test, as a test method
    private static boolean isTestFactory(Method testMethod) {
        return     AnnotationSupport.isAnnotated(testMethod, TestFactory.class)
                && !testMethod.getReturnType().equals(void.class);
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

    // we need to skip BeforeEach/AfterEach if we plan to fork as they will be executed in the forked VM, except for a test factory, see testWillFork
    // FIXME we should also do this for BeforeAll/AfterAll but the TestExecutionContext is not yet created so we don't know at this stage that we need to fork.
    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(invocationContext.getTargetClass().equals(QuickPerfTestExtension.class)){
            // We proceed with our own BeforeEach as we need it for all cases.
            // Note that currently this never happens as our BeforeEach is not intercepted by our own extension,
            // maybe it's a bug in JUnit or maybe not but we keep this as a safeguard.
            invocation.proceed();
        }
        else if (testWillFork(extensionContext)) {
            // we skip the BeforeEach if the test will fork
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if (testWillFork(extensionContext)) {
            // we skip the AfterEach if the test will fork
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    // A test factory method has no TestExecutionContext. It is executed in the test JVM, and again in the JVM of each of
    // its dynamic tests needing a specific JVM: its BeforeEach/AfterEach are executed in every JVM, so that it provides
    // the same dynamic tests in every JVM.
    private boolean testWillFork(ExtensionContext extensionContext) {
        TestExecutionContext testExecutionContext = getTestExecutionContext(extensionContext);
        return     testExecutionContext != null
                && testExecutionContext.testExecutionUsesTwoJVMs()
                && !SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        // Be careful that this method will be called by each invocation of the test template defines by a single test template method.
        // Normal lifecycle will apply.
        TestExecutionContext testExecutionContext = getTestExecutionContext(extensionContext);

        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeTestMethodInNewJvmAndRecordPerformance(invocation, invocationContext, testExecutionContext);
            return;
        }

        TestToExecuteInNewJvm testToExecuteInNewJvm = TestToExecuteInNewJvm.buildFrom(extensionContext);
        JvmOrTestIssue jvmOrTestIssue = executeTestMethodAndRecordPerformance(invocation, invocationContext.getTargetClass(), invocationContext.getExecutable(), testExecutionContext, testToExecuteInNewJvm);
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

        TestToExecuteInNewJvm testToExecuteInNewJvm = TestToExecuteInNewJvm.buildFrom(extensionContext);
        JvmOrTestIssue jvmOrTestIssue = executeTestMethodAndRecordPerformance(invocation, invocationContext.getTargetClass(), invocationContext.getExecutable(), testExecutionContext, testToExecuteInNewJvm);
        processJvmOrTestIssue(jvmOrTestIssue, testExecutionContext);
    }

    // JUnit 5.8 to 5.14, which deprecate this method, call it through the default implementation of
    // interceptDynamicTest(Invocation, DynamicTestInvocationContext, ExtensionContext).
    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        // Be careful that this method will be called by each dynamic test produced by a single test factory method.
        // The annotations of the test factory method apply to each of its dynamic tests. Each dynamic test has its own
        // TestExecutionContext, so its own recorders and working folder: the measures of the dynamic tests must not mix,
        // whether JUnit executes them one after the other or concurrently. When the annotations need a specific JVM,
        // the JVM of the dynamic test executes the test factory method again, and then only this dynamic test.
        if (isAlreadyInterceptedByAnotherInstance(extensionContext)) {
            invocation.proceed();
            return;
        }

        ExtensionContext testFactoryContext = findTestFactoryContext(extensionContext);
        Method testFactoryMethod = testFactoryContext.getRequiredTestMethod();
        TestExecutionContext testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , testFactoryMethod
                                                            , DYNAMIC_TEST_ALLOCATION_OFFSET);
        extensionContext.getStore(NAMESPACE).put(TestExecutionContext.class, testExecutionContext);
        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeDynamicTestInNewJvmAndRecordPerformance(invocation, testExecutionContext);
            return;
        }

        Class<?> testClass = testFactoryContext.getRequiredTestClass();
        TestToExecuteInNewJvm testToExecuteInNewJvm = TestToExecuteInNewJvm.buildFrom(extensionContext);
        JvmOrTestIssue jvmOrTestIssue = executeTestMethodAndRecordPerformance(invocation, testClass, testFactoryMethod, testExecutionContext, testToExecuteInNewJvm);
        processJvmOrTestIssue(jvmOrTestIssue, testExecutionContext);
    }

    // JUnit registers an extension type once, but also registers the instance of a @RegisterExtension field: so
    // QuickPerfTestExtension can be registered twice, with @QuickPerfTest and such a field for example. The first instance
    // intercepting the dynamic test executes the other one when it executes the invocation, in the test JVM or in the
    // specific JVM. Only the first instance measures the dynamic test, otherwise the recorders of both would mix: the
    // SQL recorders are registered by type, for example.
    private boolean isAlreadyInterceptedByAnotherInstance(ExtensionContext dynamicTestContext) {
        // In the specific JVM, the first instance records the invocation before it starts recording the performance: this
        // check does not allocate inside its recording
        return     TestMethodInvocationInNewJvm.INSTANCE.isTestMethodInvoked()
                || getTestExecutionContext(dynamicTestContext) != null;
    }

    // The extension contexts of a dynamic test and of a dynamic container have no test method
    private static ExtensionContext findTestFactoryContext(ExtensionContext dynamicTestContext) {
        ExtensionContext extensionContext = dynamicTestContext;
        while (!extensionContext.getTestMethod().isPresent()) {
            extensionContext = extensionContext.getParent().get();
        }
        return extensionContext;
    }

    private void processJvmOrTestIssue(JvmOrTestIssue jvmOrTestIssue, TestExecutionContext testExecutionContext) throws Throwable {
        Throwable testIssueThrowable = jvmOrTestIssue.getTestIssue().asThrowable();
        if (testIssueThrowable instanceof TestAbortedBeforeTestMethodInNewJvm) {
            // The new JVM did not invoke the test method: there is no performance record to evaluate
            testExecutionContext.cleanResources();
            TestIssue testAbort = TestIssue.buildFrom(testIssueThrowable.getCause());
            quickPerfReporter.report(JvmOrTestIssue.buildFrom(testAbort)
                                   , Collections.<PerfIssuesToFormat>emptyList()
                                   , testExecutionContext);
            return;
        }

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

    private void executeTestMethodInNewJvmAndRecordPerformance(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, TestExecutionContext testExecutionContext) throws Throwable {
        Object[] args = invocationContext.getArguments().toArray();
        Object target = invocationContext.getTarget().orElse(null);
        Method method = makeAccessible(invocationContext.getExecutable());
        invocation.skip();//skip the invocation as we directly invoke the test method

        TestMethodInvocationInNewJvm testMethodInvocation = TestMethodInvocationInNewJvm.INSTANCE;
        testMethodInvocation.recordInvocation();

        performanceRecording.start(testExecutionContext);

        try {
            //directly invoke the method to lower the interaction between JUnit, other extensions and QuickPerf.
            method.invoke(target, args);
        } catch (InvocationTargetException invocationTargetException) {
            // As JUnit, throw what the test method threw: JUnit and the other extensions handle it as usual
            Throwable testMethodThrowable = invocationTargetException.getTargetException();
            testMethodInvocation.recordThrowable(testMethodThrowable);
            throw testMethodThrowable;
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

    private void executeDynamicTestInNewJvmAndRecordPerformance(Invocation<Void> invocation, TestExecutionContext testExecutionContext) throws Throwable {
        TestMethodInvocationInNewJvm testMethodInvocation = TestMethodInvocationInNewJvm.INSTANCE;
        testMethodInvocation.recordInvocation();

        performanceRecording.start(testExecutionContext);

        try {
            // Unlike a test method, which QuickPerf directly invokes, the dynamic test is executed through the JUnit
            // invocation: the interceptors registered after QuickPerf are executed with it, and measured with it
            invocation.proceed();
        } catch (Throwable dynamicTestThrowable) {
            // As JUnit, throw what the dynamic test threw: JUnit and the other extensions handle it as usual
            testMethodInvocation.recordThrowable(dynamicTestThrowable);
            throw dynamicTestThrowable;
        } finally {
            performanceRecording.stop(testExecutionContext);
        }
    }

    private JvmOrTestIssue executeTestMethodAndRecordPerformance(Invocation<Void> invocation, Class<?> testClass, Method testMethod, TestExecutionContext testExecutionContext, TestToExecuteInNewJvm testToExecuteInNewJvm) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            JvmOrTestIssue jvmOrTestIssue = executeTestMethodInNewJvm(testClass, testMethod, testExecutionContext, testToExecuteInNewJvm);
            tryToSkipInvocation(invocation); // because the test is executed inside the 'newJvmTestLauncher'
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

    private JvmOrTestIssue executeTestMethodInNewJvm(Class<?> testClass, Method testMethod, TestExecutionContext testExecutionContext, TestToExecuteInNewJvm testToExecuteInNewJvm) {
        testToExecuteInNewJvm.saveIn(testExecutionContext.getWorkingFolder());
        NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
        JvmOrTestIssue jvmOrTestIssue = newJvmTestLauncher.executeTestMethodInNewJvm(testClass
                                                                                   , testMethod
                                                                                   , testExecutionContext
                                                                                   , QuickPerfJunit5Core.class);
        Throwable testIssueThrowable = jvmOrTestIssue.getTestIssue().asThrowable();
        if (testIssueThrowable instanceof JvmError) {
            // The new JVM did not execute the test once: there is no performance record to evaluate
            JvmIssue jvmIssue = JvmIssue.buildFrom(testIssueThrowable.getMessage());
            return JvmOrTestIssue.buildFrom(jvmIssue);
        }
        return jvmOrTestIssue;
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
