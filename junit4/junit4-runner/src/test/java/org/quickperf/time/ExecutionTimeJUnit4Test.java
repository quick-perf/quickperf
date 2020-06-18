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

package org.quickperf.time;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionTimeJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithTestHavingAnExecutionTimeGreaterThanExpected {

        @ExpectMaxExecutionTime(milliSeconds = 100)
        @Test
        public void test_during_about_one_second() throws InterruptedException {
            Thread.sleep(1_000L);
        }

    }

    @Test public void
    should_fail_if_execution_time_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = ClassWithTestHavingAnExecutionTimeGreaterThanExpected.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();
        assertThat(testResult.toString()).contains("Execution time of test method expected to be less than <100 ms> but is <");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithTestHavingAnExecutionTimeLessThanExpected {

        @ExpectMaxExecutionTime(seconds = 1)
        @Test
        public void test_during_about_one_second() throws InterruptedException {
            Thread.sleep(10);
        }

    }

    @Test public void
    should_succeed_if_execution_time_is_less_than_expected() {

        // GIVEN
        Class<?> testClass = ClassWithTestHavingAnExecutionTimeLessThanExpected.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithTestHavingAnExecutionTimeGreaterThanExpectedInASpecificJVM {

        @ExpectMaxExecutionTime(milliSeconds = 100)
        @HeapSize(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_during_about_one_second() throws InterruptedException {
            Thread.sleep(1_000L);
        }

    }

    @Test public void
    should_fail_if_execution_time_is_greater_than_expected_in_a_specific_jvm() {

        // GIVEN
        Class<?> testClass = ClassWithTestHavingAnExecutionTimeGreaterThanExpectedInASpecificJVM.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();
        assertThat(testResult.toString()).contains("Execution time of test method expected to be less than <100 ms> but is <");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithTestHavingAnExecutionTimeLessThanExpectedInASpecificJVM {

        @ExpectMaxExecutionTime(seconds = 1)
        @HeapSize(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_during_about_one_second() throws InterruptedException {
            Thread.sleep(10);
        }

    }

    @Test public void
    should_succeed_if_execution_time_is_less_than_expected_in_a_specific_jvm() {

        // GIVEN
        Class<?> testClass = ClassWithTestHavingAnExecutionTimeLessThanExpectedInASpecificJVM.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

}
