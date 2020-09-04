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

package org.quickperf.junit5;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.TestIssue;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.perfrecording.PerformanceRecording;
import org.quickperf.reporter.QuickPerfReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private final QuickPerfConfigs quickPerfConfigs =  QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    private TestExecutionContext testExecutionContext;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        int junit5AllocationOffset = 40;
        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , extensionContext.getRequiredTestMethod()
                                                            , junit5AllocationOffset);
    }

    // we need to skip all interceptor methods in case we plan to fork the VM.
    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation, ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
            // this is taken from the default implementation in org.junit.jupiter.engine.execution.ConstructorInvocation.proceed()
            return ReflectionUtils.newInstance(invocationContext.getExecutable(), invocationContext.getArguments().toArray());
        }
        else {
            return invocation.proceed();
        }
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
            // this is taken from the default implementation in org.junit.jupiter.engine.execution.MethodInvocation.proceed()
            return (T) ReflectionUtils.invokeMethod(invocationContext.getExecutable(), invocationContext.getTarget().orElse(null), invocationContext.getArguments().toArray());
        }
        else {
            return invocation.proceed();
        }
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            invocation.skip();
        }
        else {
            invocation.proceed();
        }
    }

    @Override
    public void interceptTestMethod(  Invocation<Void> invocation
                                    , ReflectiveInvocationContext<Method> invocationContext
                                    , ExtensionContext extensionContext) throws Throwable {

        if (testExecutionContext.isQuickPerfDisabled()) {
            invocation.proceed();
            return;
        }

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            executeTestMethodInNewJvmAndRecordPerformance(invocation, invocationContext);
            return;
        }

        JvmOrTestIssue jvmOrTestIssue =
                executeTestMethodAndRecordPerformance(invocation, invocationContext);

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

    private void executeTestMethodInNewJvmAndRecordPerformance(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext) throws IllegalAccessException, InvocationTargetException {
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

    private JvmOrTestIssue executeTestMethodAndRecordPerformance(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            Method testMethod = invocationContext.getExecutable();
            JvmOrTestIssue jvmOrTestIssue = executeTestMethodInNewJwm(testMethod);
            tryToSkipInvocation(invocation); // because the test method is invoked directly inside the 'newJvmTestLauncher'
            return jvmOrTestIssue;
        }
        TestIssue testIssue = executeTestMethodAndRecordPerformanceInSameJvm(invocation);
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

    private JvmOrTestIssue executeTestMethodInNewJwm(Method testMethod) {
        NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
        return newJvmTestLauncher.executeTestMethodInNewJwm(testMethod
                                                          , testExecutionContext
                                                          , QuickPerfJunit5Core.class);
    }

    private TestIssue executeTestMethodAndRecordPerformanceInSameJvm(Invocation<Void> invocation) {
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
