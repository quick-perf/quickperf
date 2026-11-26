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

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

// A JVM annotation: not a scenario shared with quick-perf-junit6, which does not start a specific JVM. The shared
// scenario QuickPerfParallelExecutionTest executes the same test in the test JVM.
class QuickPerfParallelExecutionInNewJvmTest {

    @QuickPerfTest
    static class TestFactoryExecutingASelectAfterProvidingItsDynamicTest extends SqlTestBaseJUnit5 {

        // Enables the parallel execution in the specific JVM, as a junit-platform.properties file of the class path would.
        // With a parallelism of 2, another thread can execute the dynamic test, even on a single-processor machine.
        @JvmOptions("-Djunit.jupiter.execution.parallel.enabled=true"
                 + " -Djunit.jupiter.execution.parallel.mode.default=concurrent"
                 + " -Djunit.jupiter.execution.parallel.config.strategy=fixed"
                 + " -Djunit.jupiter.execution.parallel.config.fixed.parallelism=2")
        @TestFactory @ExpectSelect(1) Stream<DynamicTest> dynamic_test_executing_one_select() {
            Thread testFactoryThread = Thread.currentThread();
            CountDownLatch dynamicTestStarted = new CountDownLatch(1);
            CountDownLatch testFactorySelectExecuted = new CountDownLatch(1);
            DynamicTest dynamicTest = dynamicTest("select", () -> {
                dynamicTestStarted.countDown();
                // Otherwise, the test would pass without the parallel execution, whatever QuickPerf does
                assertThat(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate())
                        .as("Dynamic test executed in the specific JVM").isTrue();
                assertThat(System.getProperty("junit.jupiter.execution.parallel.enabled"))
                        .as("Parallel execution enabled by a system property of the specific JVM").isEqualTo("true");
                if (Thread.currentThread() != testFactoryThread) {
                    // The test factory method goes on in its thread
                    await(testFactorySelectExecuted, "The test factory method did not execute its select");
                }
                executeOneSelect();
            });
            // JUnit closes the stream once it has provided the dynamic test. If JUnit executes the dynamic test in the thread
            // of the test factory method, it closes the stream after the dynamic test. Otherwise, the select of the test
            // factory method happens during the dynamic test.
            return Stream.of(dynamicTest).onClose(() -> {
                // The test JVM does not execute the dynamic test
                if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
                    await(dynamicTestStarted, "No thread started the dynamic test");
                    executeOneSelect();
                    testFactorySelectExecuted.countDown();
                }
            });
        }

        private void executeOneSelect() {
            EntityManager em = emf.createEntityManager();
            em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
        }

        // A timeout fails the test: the select of the test factory method could otherwise happen outside the dynamic test,
        // and the test pass without checking anything
        private static void await(CountDownLatch latch, String timeoutMessage) {
            try {
                if (!latch.await(1, TimeUnit.MINUTES)) {
                    throw new IllegalStateException(timeoutMessage);
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(interruptedException);
            }
        }
    }

    @Test
    void should_only_record_the_sql_executions_of_the_dynamic_test_in_its_jvm_having_the_parallel_execution_enabled() {

        // GIVEN
        Class<?> testClass = TestFactoryExecutingASelectAfterProvidingItsDynamicTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();
    }

}
