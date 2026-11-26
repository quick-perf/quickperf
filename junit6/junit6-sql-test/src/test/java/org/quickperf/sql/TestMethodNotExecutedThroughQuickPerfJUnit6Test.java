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
package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.Selects.executeASelect;

// An interceptor registered before QuickPerf can execute the test method itself and skip the invocation, as
// QuarkusTestExtension does: the QuickPerf interceptor is then not executed, and nothing is checked
class TestMethodNotExecutedThroughQuickPerfJUnit6Test {

    private static final String NOT_EXECUTED_THROUGH_QUICKPERF_MESSAGE =
            "The QuickPerf JUnit 6 extension (quick-perf-junit6) did not execute the test method";

    static class ExecutingTheTestMethodItself implements InvocationInterceptor {

        @Override
        public void interceptTestMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
            Method testMethod = invocationContext.getExecutable();
            testMethod.setAccessible(true);
            try {
                testMethod.invoke(invocationContext.getTarget().orElseThrow(), invocationContext.getArguments().toArray());
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            } finally {
                invocation.skip();
            }
        }

    }

    @ExtendWith(ExecutingTheTestMethodItself.class)
    @QuickPerfTest
    static class TestMethodExecutedByAnExtensionRegisteredBeforeQuickPerf {

        @ExpectSelect(2)
        @Test
        void execute_a_select() {
            executeASelect();
        }

    }

    @Test
    void should_fail_a_test_whose_method_was_not_executed_through_quickperf() {

        // WHEN
        JUnit5TestsResult result =
                JUnit5Tests.createInstance(TestMethodExecutedByAnExtensionRegisteredBeforeQuickPerf.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(errorReport).contains("ExtensionConfigurationException")
                               .contains(NOT_EXECUTED_THROUGH_QUICKPERF_MESSAGE)
                               .contains("Register QuickPerf first");

    }

    @ExtendWith(ExecutingTheTestMethodItself.class)
    @QuickPerfTest
    static class TestWithQuickPerfDisabledExecutedByAnExtensionRegisteredBeforeQuickPerf {

        @DisableQuickPerf
        @ExpectSelect(2)
        @Test
        void execute_a_select() {
            executeASelect();
        }

    }

    @Test
    void should_not_fail_a_test_with_quickperf_disabled_whose_method_was_not_executed_through_quickperf() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(
                TestWithQuickPerfDisabledExecutedByAnExtensionRegisteredBeforeQuickPerf.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isOne();

    }

    @QuickPerfTest
    @ExtendWith(ExecutingTheTestMethodItself.class)
    static class TestMethodExecutedByAnExtensionRegisteredAfterQuickPerf {

        @ExpectSelect(2)
        @Test
        void execute_a_select() {
            executeASelect();
        }

    }

    @Test
    void should_measure_a_test_method_executed_by_an_extension_registered_after_quickperf() {

        // WHEN
        JUnit5TestsResult result =
                JUnit5Tests.createInstance(TestMethodExecutedByAnExtensionRegisteredAfterQuickPerf.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                               .doesNotContain(NOT_EXECUTED_THROUGH_QUICKPERF_MESSAGE);

    }

}
