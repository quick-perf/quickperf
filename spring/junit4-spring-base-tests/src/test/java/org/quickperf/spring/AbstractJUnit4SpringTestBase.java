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

package org.quickperf.spring;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public abstract class AbstractJUnit4SpringTestBase {

    @Test public void
    a_test_throwing_an_assertion_error_and_a_performance_issue_should_fail() {

        // GIVEN
        Class<?> testClass = classWithTestHavingFunctionalAndPerfIssues();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                      .contains("java.lang.AssertionError: Performance and functional properties not respected")
                      .contains("PERFORMANCE PROPERTIES(S)")
                      .contains("FUNCTIONAL PROPERTY")
                      .contains("Failing assertion !");

    }

    protected abstract Class<?> classWithTestHavingFunctionalAndPerfIssues();

    @Test public void
    a_test_throwing_an_assertion_error_and_a_performance_issue_and_running_in_a_dedicated_jvm_should_fail() {

        // GIVEN
        Class<?> testClass = classWithTestHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                      .contains("java.lang.AssertionError: Performance and functional properties not respected")
                      .contains("PERFORMANCE PROPERTIES(S)")
                      .contains("Expected no heap allocation (test method thread) but is")
                      .contains("FUNCTIONAL PROPERTY")
                      .contains("Failing assertion !");

    }

     public abstract Class<?> classWithTestHavingFunctionalAndPerfIssuesAndRunningInADedicatedJvm();

    @Test public void
    a_test_method_having_a_performance_property_not_respected_in_a_dedicated_jvm_should_fail() {

        // GIVEN
        Class<?> testClass = aClassWithMethodNoAllocatingAndNoAllocationAnnotation();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    protected abstract Class<?> aClassWithMethodNoAllocatingAndNoAllocationAnnotation();

    @Test public void
    a_test_method_allocating_and_annotated_with_no_allocation_should_fail() {

        // GIVEN
        Class<?> testClass = aClassWithMethodAllocatingAndNoAllocationAnnotation();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(
                "a performance property is not respected");


    }

    protected abstract Class<?> aClassWithMethodAllocatingAndNoAllocationAnnotation();

    @Test public void
    disable_quick_perf_annotation_should_disable_quick_perf_features() {

        // GIVEN
        Class<?> testClass = aClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    protected abstract Class<?> aClassAnnotatedWithQPSpringRunnerAndDisableQuickPerf();

    @Test public void
    functional_iteration_annotation_should_disable_quick_perf_features() {

        // GIVEN
        Class<?> testClass = aClassAnnotatedWithQPSpringRunnerAndFunctionalIteration();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    protected abstract Class<?> aClassAnnotatedWithQPSpringRunnerAndFunctionalIteration();

    @Test public void
    execute_quick_perf_features_with_one_jvm() {

        // GIVEN
        Class<?> testClass = aClassAnnotatedWithQPSpringRunnerOneJvm();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(
                "java.lang.AssertionError: a performance property is not respected");

    }

    protected abstract Class<?> aClassAnnotatedWithQPSpringRunnerOneJvm();

    @Test public void
    two_tests_having_performance_and_functional_issues() {

        // GIVEN
        Class<?> testClass = aClassWithTwoMethodsHavingFunctionnalAndPerfIssues();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(2);

        softAssertions.assertThat(printableResult.toString())
                      .contains("java.lang.AssertionError: Performance and functional properties not respected")
                      .contains("Failing assertion of first test!")
                      .contains("Failing assertion of second test!");

        softAssertions.assertAll();

    }

    protected abstract Class<?> aClassWithTwoMethodsHavingFunctionnalAndPerfIssues();

    @Test public void
    a_failing_test_with_transactional_test_execution_listener() {

        // GIVEN
        Class<?> testClass = aClassWithTransactionalTestExecutionListenerAndAFailingTest();

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                      .contains("java.lang.AssertionError: a performance property is not respected")
                      .contains("insert")
                      .contains("into");

    }

    protected abstract Class<?> aClassWithTransactionalTestExecutionListenerAndAFailingTest();

}