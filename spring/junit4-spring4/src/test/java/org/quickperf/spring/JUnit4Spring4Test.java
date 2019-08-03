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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;
import org.quickperf.spring.database.ClassWithAFailingTestAndTransactionalTestExecutionListener;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;

public class JUnit4Spring4Test extends AbstractJUnit4SpringTestBase {

    private static class TestApplicationContextInitializer
              implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext ac) {

            // To check that the initialization of Spring Context
            // does not affect the measured allocation
            Object object = new Object();

        }

    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndWithAMethodHavingFunctionalAndPerfIssues {

        @ExpectSelect(1)
        @Test
        public void a_failing_test() {
            throw new AssertionError("Failing assertion !");
        }

    }

    @Override
    protected Class<?> classWithTestHavingFunctionalAndPerfIssues() {
        return ClassAnnotatedWithQPSpringRunnerAndWithAMethodHavingFunctionalAndPerfIssues.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndWithAMethodHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm {

        @ExpectNoHeapAllocation
        @Test
        public void a_failing_test() {
            throw new AssertionError("Failing assertion !");
        }

    }

    @Override
    public Class<?> classWithTestHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm() {
        return ClassAnnotatedWithQPSpringRunnerAndWithAMethodHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerWithAMethodHavingAPerfIssueAndRunningInADedicatedJvm {

        @ExpectNoHeapAllocation
        @Test
        public void a_test_method_without_allocation() {
            int a = 1;
        }

    }

    @Override
    protected Class<?> aClassWithMethodNoAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpringRunnerWithAMethodHavingAPerfIssueAndRunningInADedicatedJvm.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndWithATestMethodAllocatingAndAnnotatedExpectNoHeapAllocation {

        @ExpectNoHeapAllocation
        @Test
        public void a_test_method_allocating() {
            Object object = new Object();
        }

    }

    @Override
    protected Class<?> aClassWithMethodAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpringRunnerAndWithATestMethodAllocatingAndAnnotatedExpectNoHeapAllocation.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @DisableQuickPerf
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf {

        @ExpectNoHeapAllocation
        @Test
        public void a_test_method_allocating() {
            Object object = new Object();
        }

    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf() {
        return ClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @FunctionalIteration
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndFunctionalIteration {

        @ExpectNoHeapAllocation
        @Test
        public void a_test_method_allocating() {
            Object object = new Object();
        }

    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerAndFunctionalIteration() {
        return ClassAnnotatedWithQPSpringRunnerAndFunctionalIteration.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerWithATestMethodHavingAPerformanceIssueAnRunningInADedicatedJvm {

        @ExpectSelect(1)
        @Test
        public void a_test_method_not_executing_a_sql_request() {
        }

    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerOneJvm() {
        return ClassAnnotatedWithQPSpringRunnerWithATestMethodHavingAPerformanceIssueAnRunningInADedicatedJvm.class;
    }

    @RunWith(QuickPerfSpringRunner.class)
    @ContextConfiguration(initializers = TestApplicationContextInitializer.class)
    public static class ClassAnnotatedWithQPSpringRunnerAndWithTwoMethodsHavingFunctionalAndPerfIssues {

        @ExpectSelect(1)
        @Test
        public void a_first_failing_test() {
            throw new AssertionError("Failing assertion of first test!");
        }

        @ExpectSelect(1)
        @Test
        public void a_second_failing_test() {
            throw new AssertionError("Failing assertion of second test!");
        }

    }

    @Override
    protected Class<?> aClassWithTwoMethodsHavingFunctionnalAndPerfIssues() {
        return ClassAnnotatedWithQPSpringRunnerAndWithTwoMethodsHavingFunctionalAndPerfIssues.class;
    }

    @Override
    protected Class<?> aClassWithTransactionalTestExecutionListenerAndAFailingTest() {
        return ClassWithAFailingTestAndTransactionalTestExecutionListener.class;
    }

}