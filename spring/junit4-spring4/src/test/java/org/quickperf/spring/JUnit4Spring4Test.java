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

public class JUnit4Spring4Test extends AbstractJUnit4SpringTestBase {

    @Override
    protected Class<?> classWithTestHavingFunctionnalAndPerfIssues() {
        return ClassAnnotatedWithQPSpring4RunnerAndWithAMethodHavingFunctionnalAndPerfIssues.class;
    }

    @Override
    public Class<?> classWithTestHavingFunctionnalAndPerfIssuesAndRunningInADedicatedJvm() {
        return ClassAnnotatedWithQPSpring4RunnerAndWithAMethodHavingFunctionnalAndPerfIssuesAndRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithMethodNoAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpring4RunnerWithAMethodHavingAPerfIssueAndRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithMethodAllocatingAndNoAllocationAnnotation() {
        return ClassAnnotatedWithQPSpring4RunnerAndWithATestMethodAllocatingAndAnnotatedExpectNoAllocation.class;
    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf() {
        return ClassAnnotatedWithQPSpring4RunnerAndDisableQuickPerf.class;
    }

    @Override
    protected Class<?> aClassAnnotatedWithQPSpringRunnerOneJvm() {
        return ClassAnnotatedWithQPSpring4RunnerWithATestMethodHavingAPerformanceIssueAnRunningInADedicatedJvm.class;
    }

    @Override
    protected Class<?> aClassWithTwoMethodsHavingFunctionnalAndPerfIssues() {
        return ClassAnnotatedWithQPSpring4RunnerAndWithTwoMethodsHavingFunctionnalAndPerfIssues.class;
    }

}