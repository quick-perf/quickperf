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

import org.quickperf.spring.database.ClassWithAFailingTestAndTransactionalTestExecutionListener;

public class JUnit4Spring5Test extends AbstractJUnit4SpringTestBase {

    @Override
    protected Class<?> classWithTestHavingFunctionalAndPerfIssues() {
        return ClassAnnotatedWithQPSpring5RunnerAndWithAMethodHavingFunctionalAndPerfIssues.class;
    }

    @Override
    public Class<?> classWithTestHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm() {
        return ClassAnnotatedWithQPSpring5RunnerAndWithAMethodHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithMethodNoAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpring5RunnerWithAMethodHavingAPerfIssueAndRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithMethodAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpring5RunnerAndWithATestMethodAllocatingAndAnnotatedExpectNoHeapAllocation.class;
    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf() {
        return ClassAnnotatedWithQPSpring5RunnerAndDisableQuickPerf.class;
    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerAndFunctionalIteration() {
        return ClassAnnotatedWithQPSpring5RunnerAndFunctionalIteration.class;
    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerOneJvm() {
        return ClassAnnotatedWithQPSpring5RunnerWithATestMethodHavingAPerformanceIssueAnRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithTwoMethodsHavingFunctionnalAndPerfIssues() {
        return ClassAnnotatedWithQPSpring5RunnerAndWithTwoMethodsHavingFunctionalAndPerfIssues.class;
    }

    @Override
    protected Class<?> aClassWithTransactionalTestExecutionListenerAndAFailingTest() {
        return ClassWithAFailingTestAndTransactionalTestExecutionListener.class;
    }

}