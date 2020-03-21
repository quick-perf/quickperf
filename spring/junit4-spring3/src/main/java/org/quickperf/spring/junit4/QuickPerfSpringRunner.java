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

package org.quickperf.spring.junit4;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.*;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.AnnotationsExtractor;
import org.quickperf.TestExecutionContext;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.junit4.MainJvmAfterJUnitStatement;
import org.quickperf.SystemProperties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class QuickPerfSpringRunner extends BlockJUnit4ClassRunner {

    // Only used if test method is executed in a dedicated JVM
    private static SpringRunnerWithQuickPerfFeatures QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM;

    private SpringRunnerWithQuickPerfFeatures springRunnerWithQuickPerfFeatures;

    private SpringRunnerWithCallableProtectedMethods springRunner;

    private QuickPerfConfigs quickPerfConfigs;

    private final Class<?> testClass;

    private boolean quickPerfFeaturesAreDisabled;

    private static final Statement NO_STATEMENT = new Statement() {
        @Override
        public void evaluate() {}
    };

    private TestExecutionContext testExecutionContext;

    private boolean testMethodToBeLaunchedInASpecificJvm;

    public QuickPerfSpringRunner(Class<?> klass) throws InitializationError {
        super(init(klass));
        if (!SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();
        }
        this.testClass = klass;
    }

    private static Class<?> init(Class<?> klass) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM = SpringRunnerWithQuickPerfFeatures.build(klass);
        }
        return klass;
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {

        Method testMethod = frameworkMethod.getMethod();

        if(quickPerfFeaturesAreDisabled) {
            return springRunner.methodInvoker(frameworkMethod, test);
        }

        if (      testMethodToBeLaunchedInASpecificJvm
              && !SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            testExecutionContext = TestExecutionContext.buildNewJvmFrom(quickPerfConfigs, testMethod);
            return NO_STATEMENT;
        }

        int runnerAllocationOffset = 0;
        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , testMethod
                                                            , runnerAllocationOffset);

        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.methodInvoker(frameworkMethod, test);
        }

        return springRunnerWithQuickPerfFeatures.methodInvoker(frameworkMethod, test);

    }

    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.withBefores(frameworkMethod, testInstance, statement);
        }
        if (     testMethodToBeLaunchedInASpecificJvm
             && !SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return NO_STATEMENT;
        }
        if(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.withBefores(frameworkMethod, testInstance, statement);
        }
        return springRunnerWithQuickPerfFeatures.withBefores(frameworkMethod, testInstance, statement);
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.withAfters(frameworkMethod, testInstance, statement);
        }
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.withAfters(frameworkMethod, testInstance, statement);
        }
        if(testMethodToBeLaunchedInASpecificJvm) {
            return new MainJvmAfterJUnitStatement(frameworkMethod
                                                , testExecutionContext
                                                , quickPerfConfigs
                                                , NO_STATEMENT);
        }
        // The test method is not executed in a specific JVM and performance properties
        // are evaluated
        return springRunnerWithQuickPerfFeatures.withAfters(frameworkMethod, testInstance, statement);
    }

    @Override
    public Description getDescription() {
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.getDescription();
        }
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.getDescription();
        }
        return super.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        if(quickPerfFeaturesAreDisabled) {
            springRunner.run(notifier);
            return;
        }
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.run(notifier);
        } else {
            super.run(notifier);
        }
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.withBeforeClasses(statement);
        }
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.withBeforeClasses(statement);
        }
        return super.withBeforeClasses(statement);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.withAfterClasses(statement);
        }
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.withAfterClasses(statement);
        }
        return super.withAfterClasses(statement);
    }

    @Override
    protected Object createTest() throws Exception {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.createTest();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.createTest();
        }
        return super.createTest();
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {

        Annotation[] annotations = retrieveAnnotations(frameworkMethod);

        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.runChild(frameworkMethod, notifier);
        } else if(quickPerfFeaturesAreDisabled(annotations)) {
            this.quickPerfFeaturesAreDisabled = true;
            this.springRunner = SpringRunnerWithCallableProtectedMethods
                    .buildSpringRunner(testClass);
            springRunner.runChild(frameworkMethod, notifier);
        } else {
            SetOfAnnotationConfigs testAnnotationConfigs = this.quickPerfConfigs.getTestAnnotationConfigs();
            this.testMethodToBeLaunchedInASpecificJvm = testAnnotationConfigs.hasTestMethodToBeLaunchedInASpecificJvmWith(annotations);

            if (testMethodToBeLaunchedInASpecificJvm) {
                super.runChild(frameworkMethod, notifier);
            } else {
                Method method = frameworkMethod.getMethod();
                Class<?> testClass = method.getDeclaringClass();
                this.springRunnerWithQuickPerfFeatures =
                        SpringRunnerWithQuickPerfFeatures.build(testClass);
                springRunnerWithQuickPerfFeatures.runChild(frameworkMethod, notifier);
            }

        }
    }

    private Annotation[] retrieveAnnotations(FrameworkMethod frameworkMethod) {

        QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();
        SetOfAnnotationConfigs setOfAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();

        Method method = frameworkMethod.getMethod();

        AnnotationsExtractor annotationsExtractor = AnnotationsExtractor.INSTANCE;
        return annotationsExtractor.extractAnnotationsFor(method, setOfAnnotationConfigs);

    }

    private static boolean quickPerfFeaturesAreDisabled(Annotation[] perfAnnotations) {
        for (Annotation perfAnnotation : perfAnnotations) {
            if(    perfAnnotation.annotationType().equals(DisableQuickPerf.class)
                || perfAnnotation.annotationType().equals(FunctionalIteration.class)
            ) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
        }
        return super.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Statement withPotentialTimeout(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.withPotentialTimeout(frameworkMethod, testInstance, next);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.withPotentialTimeout(frameworkMethod, testInstance, next);
        }
        return super.withPotentialTimeout(frameworkMethod, testInstance, next);
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.isIgnored(child);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.isIgnored(child);
        }
        return super.isIgnored(child);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.describeChild(method);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.describeChild(method);
        }
        return super.describeChild(method);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.getChildren();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.getChildren();
        }
        return super.getChildren();
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.computeTestMethods();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.computeTestMethods();
        }
        return super.computeTestMethods();
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.collectInitializationErrors(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.collectInitializationErrors(errors);
        } else {
            super.collectInitializationErrors(errors);
        }
    }

    @Override
    protected void validateNoNonStaticInnerClass(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateNoNonStaticInnerClass(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateNoNonStaticInnerClass(errors);
        } else {
            super.validateNoNonStaticInnerClass(errors);
        }
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateConstructor(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateConstructor(errors);
        } else {
            super.validateConstructor(errors);
        }
    }

    @Override
    protected void validateOnlyOneConstructor(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateOnlyOneConstructor(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateOnlyOneConstructor(errors);
        } else {
            super.validateOnlyOneConstructor(errors);
        }
    }

    @Override
    protected void validateZeroArgConstructor(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateZeroArgConstructor(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateZeroArgConstructor(errors);
        } else {
            super.validateZeroArgConstructor(errors);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateInstanceMethods(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateInstanceMethods(errors);
        } else {
            super.validateInstanceMethods(errors);
        }
    }

    @Override
    protected void validateFields(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateFields(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateFields(errors);
        } else {
            super.validateFields(errors);
        }
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validateTestMethods(errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validateTestMethods(errors);
        } else {
            super.validateTestMethods(errors);
        }
    }

    @Override
    protected String testName(FrameworkMethod method) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.testName(method);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.testName(method);
        }
        return super.testName(method);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.methodBlock(method);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.methodBlock(method);
        }

        if(testMethodToBeLaunchedInASpecificJvm) {
            return super.methodBlock(method);
        }

        return springRunnerWithQuickPerfFeatures.methodBlock(method);

    }

    @Override
    protected List<MethodRule> rules(Object target) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.rules(target);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.rules(target);
        }
        return super.rules(target);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.getTestRules(target);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.getTestRules(target);
        }
        return super.getTestRules(target);
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.createTestClass(testClass);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.createTestClass(testClass);
        }
        return super.createTestClass(testClass);
    }

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
        } else if(quickPerfFeaturesAreDisabled) {
            springRunner.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
        } else {
            super.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
        }
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.classBlock(notifier);
        }
        return super.classBlock(notifier);
    }

    @Override
    protected List<TestRule> classRules() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.classRules();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.classRules();
        }
        return super.classRules();
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.childrenInvoker(notifier);
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.childrenInvoker(notifier);
        }
        return super.childrenInvoker(notifier);
    }

    @Override
    protected String getName() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.getName();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.getName();
        }
        return super.getName();
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.getRunnerAnnotations();
        }
        if(quickPerfFeaturesAreDisabled) {
            return springRunner.getRunnerAnnotations();
        }
        return super.getRunnerAnnotations();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.filter(filter);
        } else if (quickPerfFeaturesAreDisabled){
            springRunner.filter(filter);
        } else {
            super.filter(filter);
        }
    }

    @Override
    public void sort(Sorter sorter) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.sort(sorter);
        } else if (quickPerfFeaturesAreDisabled){
            springRunner.sort(sorter);
        } else {
            springRunnerWithQuickPerfFeatures.sort(sorter);
        }
    }

    @Override
    public void setScheduler(RunnerScheduler scheduler) {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.setScheduler(scheduler);
        }  else if (quickPerfFeaturesAreDisabled) {
            springRunner.setScheduler(scheduler);
        } else {
            springRunnerWithQuickPerfFeatures.setScheduler(scheduler);
        }
    }

    @Override
    public int testCount() {
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return QUICK_PERF_SPRING_RUNNER_FOR_SPECIFIC_JVM.testCount();
        }
        if (quickPerfFeaturesAreDisabled) {
            return springRunner.testCount();
        }
        return springRunnerWithQuickPerfFeatures.testCount();
    }

}
