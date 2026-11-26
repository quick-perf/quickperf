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
package org.quickperf.junit6;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.platform.commons.support.AnnotationSupport;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.perfrecording.TestThreads;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * The QuickPerf extension for JUnit 6. It executes the QuickPerf SQL annotations and the core annotations, such as
 * {@code @ExpectMaxExecutionTime}, in the JVM running JUnit, and never starts a dedicated JVM for a test.
 * <p>
 * It therefore never applies the annotations that need a dedicated JVM, such as {@code @HeapSize},
 * {@code @MeasureHeapAllocation} or {@code @ProfileJvm}. A test that has one on its method or class fails, and a global
 * one is ignored with a warning.
 * <p>
 * Register it with {@link QuickPerfTest} declared before the other extension annotations, with the autodetection of
 * extensions, or with a static {@code @RegisterExtension} field. Register only one QuickPerf extension for a test. The
 * SQL annotations need a data source proxied by QuickPerf.
 */
public class QuickPerfTestExtension implements TestInstancePreConstructCallback, BeforeAllCallback
                                             , BeforeClassTemplateInvocationCallback, BeforeEachCallback
                                             , BeforeTestExecutionCallback, AfterEachCallback, InvocationInterceptor {

    static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(QuickPerfTestExtension.class);

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    // The callbacks and interceptors of this extension mark the threads executing the code of the test class as test
    // threads, even if QuickPerf is disabled for the test: without that, the SQL of this code would be recorded for the
    // tests recording at the same time. JUnit may execute this code on several threads: with concurrent execution, the
    // class, each invocation of a @ParameterizedClass and each test can be executed by different threads; with
    // @Timeout(threadMode = SEPARATE_THREAD), each lifecycle method and test method is executed on a thread started for
    // it. Each hook that can be the first one called on a thread marks it first, before any early return.
    // beforeTestExecution and afterEach are called on the thread of beforeEach, which marks it.

    // JUnit creates the test instance before calling beforeAll with @TestInstance(PER_CLASS), and before calling
    // beforeEach otherwise. Called before JUnit resolves the parameters of the constructor, and also when a
    // TestInstanceFactory creates the test instance. SpringExtension loads its ApplicationContext when it resolves a
    // constructor parameter, or when it post-processes the test instance: Hibernate or Flyway may execute SQL when the
    // context starts.
    @Override
    public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
        TestThreads.markCurrentThread();
    }

    // An interceptor registered before this one may execute the constructor on another thread
    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation
                                             , ReflectiveInvocationContext<Constructor<T>> invocationContext
                                             , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        return invocation.proceed();
    }

    // Before the beforeAll callbacks of the extensions registered after this one, and the @BeforeAll methods. With
    // quick-perf-junit5 registered before this extension, the class fails before any context is built.
    @Override
    public void beforeAll(ExtensionContext context) {
        TestThreads.markCurrentThread();
        if (!isInJvmForkedByQuickPerf() && !quickPerfIsDisabledForTheClass(context.getRequiredTestClass())) {
            JUnit5ExtensionDetector.failIfRegisteredFor(context);
        }
    }

    // With QuickPerf disabled, neither extension records SQL: both can be registered. A test method disabling QuickPerf
    // is only known in beforeEach.
    private static boolean quickPerfIsDisabledForTheClass(Class<?> testClass) {
        return     SystemProperties.QUICK_PERF_DISABLED.evaluate()
                || testClass.isAnnotationPresent(DisableQuickPerf.class)
                || testClass.isAnnotationPresent(FunctionalIteration.class);
    }

    // The @BeforeParameterizedClassInvocation methods of a @ParameterizedClass are executed by a callback of each class
    // invocation, which no interceptor sees. This callback, registered on the class, is called before it.
    @Override
    public void beforeClassTemplateInvocation(ExtensionContext context) {
        TestThreads.markCurrentThread();
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        invocation.proceed();
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        invocation.proceed();
    }

    // Before the beforeEach callbacks of the extensions registered after this one, and the @BeforeEach methods. The
    // context is built here, and not when the test method is intercepted: @DisplaySql registers its recorder when the
    // context is built, to display the SQL of the @BeforeEach methods too.
    @Override
    public void beforeEach(ExtensionContext context) {
        TestThreads.markCurrentThread();
        if (isInJvmForkedByQuickPerf() || findTestRecording(context) != null) {
            // quick-perf-junit5 owns the JVMs it starts, or another instance of this extension records the test
            return;
        }
        Method testMethod = context.getRequiredTestMethod();
        PerfAnnotations perfAnnotations = PerfAnnotations.extractFor(quickPerfConfigs, testMethod);
        if (!perfAnnotations.quickPerfDisabled()) {
            // quick-perf-junit5 registered on the class after this extension, or registered before this extension when
            // this one has no beforeAll callback: registered on the test method, or in a non-static @RegisterExtension
            // field
            JUnit5ExtensionDetector.failIfRegisteredFor(context);
        }
        // Before anything is built, for a test factory too: it then fails once, before its dynamic tests
        JvmAnnotations.failOrWarn(perfAnnotations, context);
        if (isTestFactory(testMethod)) {
            // Each dynamic test gets its own context, see interceptDynamicTest. QuickPerf measures neither the test
            // factory method nor its @BeforeEach and @AfterEach methods.
            return;
        }
        TestExecutionContext testExecutionContext =
                TestExecutionContext.buildInTestJvmFrom(quickPerfConfigs, perfAnnotations.annotationsAppliedInTestJvm());
        context.getStore(NAMESPACE).put(TestRecording.class, new TestRecording(this, testExecutionContext));
    }

    // After every beforeEach callback, that of quick-perf-junit5 registered on the test method included, and before the
    // interceptors. Unlike an exception thrown by an interceptor, an exception thrown here never reaches the
    // TestExecutionExceptionHandler extensions, which could report a test that was never executed as successful.
    @Override
    public void beforeTestExecution(ExtensionContext context) {
        if (   isRecordedByThisInstance(context)
            && !findTestRecording(context).testExecutionContext().isQuickPerfDisabled()) {
            JUnit5ExtensionDetector.failIfRegisteredFor(context);
        }
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation
                                        , ReflectiveInvocationContext<Method> invocationContext
                                        , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        bindRecordersToCurrentThread(extensionContext);
        invocation.proceed();
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        bindRecordersToCurrentThread(extensionContext);
        invocation.proceed();
    }

    // With @Timeout(threadMode = SEPARATE_THREAD), JUnit executes a @BeforeEach or @AfterEach method on a thread
    // started for it, and then stops this thread: the recorders registered when the context was built (@DisplaySql,
    // @ProfileConnection) also record this thread, until it ends. Does nothing on the thread that built the context.
    private void bindRecordersToCurrentThread(ExtensionContext context) {
        if (isRecordedByThisInstance(context)) {
            findTestRecording(context).testExecutionContext().bindRecordersToCurrentThread();
        }
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation
                                          , ReflectiveInvocationContext<Method> invocationContext
                                          , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        return invocation.proceed();
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation
                                  , ReflectiveInvocationContext<Method> invocationContext
                                  , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        executeTestMethod(invocation, extensionContext);
    }

    // Called for each invocation of a test template method, such as a @RepeatedTest or a @ParameterizedTest method:
    // each invocation has its own context, built in beforeEach
    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation
                                          , ReflectiveInvocationContext<Method> invocationContext
                                          , ExtensionContext extensionContext) throws Throwable {
        TestThreads.markCurrentThread();
        executeTestMethod(invocation, extensionContext);
    }

    private void executeTestMethod(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        TestRecording testRecording = findTestRecording(extensionContext);
        if (   testRecording == null
            || !testRecording.isRecordedBy(this)
            || testRecording.testExecutionContext().isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }
        executeAndCheckPerformance(invocation, testRecording);
    }

    // Called for each dynamic test of a test factory method. The annotations of the test factory method apply to each
    // of its dynamic tests. Each dynamic test has its own context: the measures of the dynamic tests must not mix,
    // whether JUnit executes them one after the other or concurrently. A dynamic test gets no beforeEach and afterEach
    // callbacks: its context is built and released here, on this thread. Must not call super: the default
    // implementation proceeds with the invocation.
    @Override
    public void interceptDynamicTest(Invocation<Void> invocation
                                   , DynamicTestInvocationContext invocationContext
                                   , ExtensionContext dynamicTestContext) throws Throwable {
        TestThreads.markCurrentThread();
        if (isInJvmForkedByQuickPerf() || findTestRecording(dynamicTestContext) != null) {
            invocation.proceed();
            return;
        }
        Method testFactoryMethod = findTestFactoryContext(dynamicTestContext).getRequiredTestMethod();
        PerfAnnotations perfAnnotations = PerfAnnotations.extractFor(quickPerfConfigs, testFactoryMethod);
        if (!perfAnnotations.quickPerfDisabled()) {
            // Throws before proceeding: the dynamic test is not executed
            JUnit5ExtensionDetector.failIfRegisteredFor(dynamicTestContext);
        }
        // beforeEach has already rejected the annotations of the test factory needing a specific JVM: only warns
        JvmAnnotations.failOrWarn(perfAnnotations, dynamicTestContext);
        TestExecutionContext testExecutionContext =
                TestExecutionContext.buildInTestJvmFrom(quickPerfConfigs, perfAnnotations.annotationsAppliedInTestJvm());
        TestRecording testRecording = new TestRecording(this, testExecutionContext);
        dynamicTestContext.getStore(NAMESPACE).put(TestRecording.class, testRecording);
        try {
            if (testExecutionContext.isQuickPerfDisabled()) {
                invocation.proceed();
            } else {
                executeAndCheckPerformance(invocation, testRecording);
            }
        } finally {
            testExecutionContext.cleanResources();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (!isRecordedByThisInstance(context)) {
            return;
        }
        TestRecording testRecording = findTestRecording(context);
        // On the thread that executed beforeEach and built the context, after the @AfterEach methods: the recorders
        // that registered themselves when the context was built (@DisplaySql, @ProfileConnection) also record the
        // @AfterEach methods. JUnit calls afterEach even when the test method was not executed, after a failure or an
        // abort in a @BeforeEach method for example, or was executed on another thread, with
        // @Timeout(threadMode = SEPARATE_THREAD) for example.
        TestExecutionContext testExecutionContext = testRecording.testExecutionContext();
        testExecutionContext.cleanResources();
        // A test with no annotation applied has no performance property that could go unchecked
        boolean notExecutedThroughQuickPerf =    !testRecording.isTestMethodExecuted()
                                              && !testExecutionContext.isQuickPerfDisabled()
                                              && testExecutionContext.getPerfAnnotations().length > 0
                                              && context.getExecutionException().isEmpty();
        if (notExecutedThroughQuickPerf) {
            // An extension registered before this one executed or skipped the test method without proceeding with the
            // invocation, as QuarkusTestExtension does: the test would pass with no performance property checked
            throw new ExtensionConfigurationException(Messages.notExecutedThroughQuickPerf());
        }
    }

    private void executeAndCheckPerformance(Invocation<Void> invocation, TestRecording testRecording) throws Throwable {
        testRecording.markTestMethodExecuted();
        // Through the invocation, not by reflection: the interceptors registered after this extension are executed, and
        // the stack traces of @ProfileConnection end at the test method
        PerformanceCheck.executeAndCheck(testRecording.testExecutionContext()
                                       , quickPerfConfigs
                                       , invocation);
    }

    // quick-perf-junit5 owns the JVMs it starts, where the SQL recorders are not per thread: this extension must not
    // record there
    private static boolean isInJvmForkedByQuickPerf() {
        return SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
    }

    private static TestRecording findTestRecording(ExtensionContext context) {
        return context.getStore(NAMESPACE).get(TestRecording.class, TestRecording.class);
    }

    private boolean isRecordedByThisInstance(ExtensionContext context) {
        TestRecording testRecording = findTestRecording(context);
        return testRecording != null && testRecording.isRecordedBy(this);
    }

    // As JUnit, which executes a method returning void, and annotated with @TestFactory and @Test, as a test method
    private static boolean isTestFactory(Method testMethod) {
        return     AnnotationSupport.isAnnotated(testMethod, TestFactory.class)
                && !testMethod.getReturnType().equals(void.class);
    }

    // The extension contexts of a dynamic test and of a dynamic container have no test method
    private static ExtensionContext findTestFactoryContext(ExtensionContext dynamicTestContext) {
        ExtensionContext extensionContext = dynamicTestContext;
        while (extensionContext.getTestMethod().isEmpty()) {
            extensionContext = extensionContext.getParent().orElseThrow();
        }
        return extensionContext;
    }

}
