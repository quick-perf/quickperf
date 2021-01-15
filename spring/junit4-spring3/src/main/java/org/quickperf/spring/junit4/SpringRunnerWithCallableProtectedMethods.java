/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.spring.junit4;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.annotation.Annotation;
import java.util.List;

class SpringRunnerWithCallableProtectedMethods extends SpringJUnit4ClassRunner {

    static SpringRunnerWithCallableProtectedMethods buildSpringRunner(Class<?> testClass) {
        try {
            return new SpringRunnerWithCallableProtectedMethods(testClass);
        } catch (InitializationError initializationError) {
            throw new IllegalStateException(initializationError);
        }
    }

    private SpringRunnerWithCallableProtectedMethods(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        super.runChild(frameworkMethod, notifier);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        return super.methodInvoker(method, test);
    }

    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return super.withBefores(frameworkMethod, testInstance, statement);
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return super.withAfters(frameworkMethod, testInstance, statement);
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
    protected Statement possiblyExpectingExceptions(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        return super.possiblyExpectingExceptions(frameworkMethod, testInstance, next);
    }

    @Override
    @SuppressWarnings("deprecation")
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
    protected List<FrameworkMethod> computeTestMethods() {
        return super.computeTestMethods();
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

}