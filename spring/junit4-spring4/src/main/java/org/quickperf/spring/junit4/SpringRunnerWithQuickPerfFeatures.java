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

import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.*;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Annotation;
import java.util.List;


class SpringRunnerWithQuickPerfFeatures extends SpringJUnit4ClassRunner {

    private final QuickPerfJUnitRunner quickPerfJUnitRunner;

    static SpringRunnerWithQuickPerfFeatures build(Class<?> testClass) {
        try {
            return new SpringRunnerWithQuickPerfFeatures(testClass);
        } catch (InitializationError initializationError) {
            throw new IllegalStateException(initializationError);
        }
    }

    private SpringRunnerWithQuickPerfFeatures(Class<?> clazz) throws InitializationError {
        super(clazz);
        quickPerfJUnitRunner = new QuickPerfJUnitRunner(clazz);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Test.class);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {
        return quickPerfJUnitRunner.methodInvoker(frameworkMethod, test);
    }

    @Override
    public Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Statement springBefores = super.withBefores(frameworkMethod, testInstance, statement);
        return quickPerfJUnitRunner.withBefores(frameworkMethod, testInstance, springBefores);
    }

    @Override
    public Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Statement springAfters = super.withAfters(frameworkMethod, testInstance, statement);
        return quickPerfJUnitRunner.withAfters(frameworkMethod, testInstance, springAfters);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        return super.withBeforeClasses(statement);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        return super.withAfterClasses(statement);
    }

    @Override
    protected Object createTest() throws Exception {
        return super.createTest();
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        super.runChild(frameworkMethod, notifier);
    }

    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        return super.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
    }

    @Override
    protected Statement withPotentialTimeout(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        return super.withPotentialTimeout(frameworkMethod, testInstance, next);
    }

    @Override
    protected boolean isIgnored(FrameworkMethod child) {
        return super.isIgnored(child);
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        return super.describeChild(method);
    }

    @Override
    protected List<FrameworkMethod> getChildren() {
        return super.getChildren();
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        super.collectInitializationErrors(errors);
    }

    @Override
    protected void validateNoNonStaticInnerClass(List<Throwable> errors) {
        super.validateNoNonStaticInnerClass(errors);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
        super.validateConstructor(errors);
    }

    @Override
    protected void validateOnlyOneConstructor(List<Throwable> errors) {
        super.validateOnlyOneConstructor(errors);
    }

    @Override
    protected void validateZeroArgConstructor(List<Throwable> errors) {
        super.validateZeroArgConstructor(errors);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void validateInstanceMethods(List<Throwable> errors) {
        super.validateInstanceMethods(errors);
    }

    @Override
    protected void validateFields(List<Throwable> errors) {
        super.validateFields(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        super.validateTestMethods(errors);
    }

    @Override
    protected String testName(FrameworkMethod method) {
        return super.testName(method);
    }

    @Override
    protected Statement methodBlock(FrameworkMethod method) {
        return super.methodBlock(method);
    }

    @Override
    protected List<MethodRule> rules(Object target) {
        return super.rules(target);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        return super.getTestRules(target);
    }

    @Override
    protected TestClass createTestClass(Class<?> testClass) {
        return super.createTestClass(testClass);
    }

    @Override
    protected void validatePublicVoidNoArgMethods(Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors) {
        super.validatePublicVoidNoArgMethods(annotation, isStatic, errors);
    }

    @Override
    protected Statement classBlock(RunNotifier notifier) {
        return super.classBlock(notifier);
    }

    @Override
    protected List<TestRule> classRules() {
        return super.classRules();
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        return super.childrenInvoker(notifier);
    }

    @Override
    protected String getName() {
        return super.getName();
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
        return super.getRunnerAnnotations();
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        super.filter(filter);
    }

    @Override
    public void sort(Sorter sorter) {
        super.sort(sorter);
    }

    @Override
    public void setScheduler(RunnerScheduler scheduler) {
        super.setScheduler(scheduler);
    }

    @Override
    public int testCount() {
        return super.testCount();
    }

}
