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
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.sql.annotation.ExpectSelect;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.Selects.executeSelects;

// A @ParameterizedClass (JUnit 5.13+) executes its tests once per argument, each time with a new instance
class ParameterizedClassJUnit6Test {

    private static final String CONSTRUCTOR_THREAD = "constructor-thread";

    // Declared before QuickPerf: the QuickPerf interceptor executes the constructor on the new thread, and must mark it
    static class ConstructorOnANewThread implements InvocationInterceptor {

        @Override
        public <T> T interceptTestClassConstructor(Invocation<T> invocation
                                                 , ReflectiveInvocationContext<Constructor<T>> invocationContext
                                                 , ExtensionContext extensionContext) throws Throwable {
            AtomicReference<T> testInstance = new AtomicReference<>();
            AtomicReference<Throwable> throwable = new AtomicReference<>();
            Thread constructorThread = new Thread(() -> {
                try {
                    testInstance.set(invocation.proceed());
                } catch (Throwable constructorThrowable) {
                    throwable.set(constructorThrowable);
                }
            }, CONSTRUCTOR_THREAD);
            constructorThread.start();
            constructorThread.join();
            if (throwable.get() != null) {
                throw throwable.get();
            }
            return testInstance.get();
        }

    }

    @ExtendWith(ConstructorOnANewThread.class)
    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2})
    static class ParameterizedClassExecutingSelects {

        @Parameter
        int numberOfSelects;

        public ParameterizedClassExecutingSelects() {
            assertThat(Thread.currentThread().getName()).isEqualTo(CONSTRUCTOR_THREAD);
            assertThat(TestThreads.isCurrentThreadMarked()).as("constructor thread marked as a test thread").isTrue();
        }

        @ExpectSelect(1)
        @Test
        void execute_the_selects() {
            executeSelects(numberOfSelects);
        }

        @ExpectSelect(2)
        @Test
        void execute_one_more_select() {
            executeSelects(numberOfSelects + 1);
        }

    }

    // Each test method of each class invocation has its own context: with a context shared by the tests, the selects of
    // the tests would add up
    @Test
    void should_check_each_test_of_each_invocation_of_a_parameterized_class() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(ParameterizedClassExecutingSelects.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfSucceededTests()).as(errorReport).isEqualTo(2);
        assertThat(result.getNumberOfFailures()).as(errorReport).isEqualTo(2);
        assertThat(errorReport).contains("You may think that <1> select statement")
                               .contains("But there are in fact <2>...")
                               .contains("You may think that <2> select statements")
                               .contains("But there are in fact <3>...");

    }

}
