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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class JUnit6ExtensionTest {

    @QuickPerfTest
    static class JUnit6MethodFailing {

        @Test
        public void a_failing_test() {
            assertThat(false).isTrue();
        }

    }

    @Test
    void a_test_with_failing_business_code_should_fail() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(JUnit6MethodFailing.class);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expecting")
                               .contains("false")
                               .contains("true")
                               .doesNotContain("performance");

    }

    @QuickPerfTest
    static class TestWithLifecycleMethods {

        static final AtomicInteger BEFORE_ALL = new AtomicInteger();
        static final AtomicInteger BEFORE_EACH = new AtomicInteger();
        static final AtomicInteger TEST_METHOD = new AtomicInteger();
        static final AtomicInteger AFTER_EACH = new AtomicInteger();
        static final AtomicInteger AFTER_ALL = new AtomicInteger();

        @BeforeAll
        static void before_all() {
            BEFORE_ALL.incrementAndGet();
        }

        @BeforeEach
        void before_each() {
            BEFORE_EACH.incrementAndGet();
        }

        @MeasureExecutionTime
        @Test
        void a_test() {
            TEST_METHOD.incrementAndGet();
        }

        @AfterEach
        void after_each() {
            AFTER_EACH.incrementAndGet();
        }

        @AfterAll
        static void after_all() {
            AFTER_ALL.incrementAndGet();
        }

    }

    @Test
    void should_execute_the_lifecycle_methods_and_the_test_method_once() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(TestWithLifecycleMethods.class);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).as(jUnit5TestsResult.getErrorReport()).isZero();
        assertThat(TestWithLifecycleMethods.BEFORE_ALL.get()).as("@BeforeAll executions").isOne();
        assertThat(TestWithLifecycleMethods.BEFORE_EACH.get()).as("@BeforeEach executions").isOne();
        assertThat(TestWithLifecycleMethods.TEST_METHOD.get()).as("test method executions").isOne();
        assertThat(TestWithLifecycleMethods.AFTER_EACH.get()).as("@AfterEach executions").isOne();
        assertThat(TestWithLifecycleMethods.AFTER_ALL.get()).as("@AfterAll executions").isOne();

    }

    // Executes the test method itself and skips the invocation, as QuarkusTestExtension does
    static class ExecutingTheTestMethodItself implements InvocationInterceptor {

        @Override
        public void interceptTestMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
            Method testMethod = invocationContext.getExecutable();
            testMethod.setAccessible(true);
            try {
                testMethod.invoke(invocationContext.getTarget().orElseThrow()
                                , invocationContext.getArguments().toArray());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } finally {
                invocation.skip();
            }
        }

    }

    // No global annotation in this module: no annotation applies to the test
    @ExtendWith(ExecutingTheTestMethodItself.class)
    @QuickPerfTest
    static class TestWithoutAnnotationExecutedByAnExtensionRegisteredBeforeQuickPerf {

        @Test
        void a_test() { }

    }

    @Test
    void should_not_fail_a_test_without_annotation_whose_method_was_not_executed_through_quickperf() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(
                TestWithoutAnnotationExecutedByAnExtensionRegisteredBeforeQuickPerf.class).run();

        // THEN: no performance property could go unchecked
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isOne();

    }

    @QuickPerfTest
    static class SlowTest {

        @ExpectMaxExecutionTime(milliSeconds = 10)
        @Test
        void a_test_during_about_200_ms() throws InterruptedException {
            Thread.sleep(200);
        }

    }

    @Test
    void should_fail_if_a_core_performance_property_is_not_respected() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(SlowTest.class);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();
        assertThat(jUnit5TestsResult.getErrorReport()).contains("a performance-related property is not respected")
                                                      .contains("Execution time of the test method expected to be less than");

    }

}
