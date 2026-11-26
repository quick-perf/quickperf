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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.perfrecording.TestThreads;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

// The SQL executed on a thread that QuickPerf has not marked as a test thread is recorded for the tests recording at
// the same time. Each test runs on a new thread: a thread used by a previous test would already be marked.
class TestThreadsMarkingTest {

    // Resolves a parameter of the test class constructor, as SpringExtension does, which then loads its
    // ApplicationContext: Hibernate or Flyway may execute SQL
    static class ConstructorParameterResolver implements ParameterResolver {

        static final AtomicReference<Boolean> THREAD_MARKED = new AtomicReference<>();

        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == String.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            THREAD_MARKED.set(TestThreads.isCurrentThreadMarked());
            return "resolved";
        }

    }

    // With PER_CLASS, JUnit creates the test instance before calling beforeAll
    @QuickPerfTest
    @ExtendWith(ConstructorParameterResolver.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    static class TestWithAConstructorParameter {

        TestWithAConstructorParameter(String parameter) { }

        @Test
        void a_test() { }

    }

    @Test
    void should_mark_the_thread_resolving_the_parameters_of_the_test_class_constructor() throws InterruptedException {

        // WHEN
        JUnit5TestsResult result = runOnANewThread(() -> JUnit5Tests.createInstance(TestWithAConstructorParameter.class)
                                                                    .run());

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(ConstructorParameterResolver.THREAD_MARKED.get())
                .as("thread resolving the constructor parameter marked as a test thread")
                .isTrue();

    }

    // A callback of each invocation of a @ParameterizedClass executes its @BeforeParameterizedClassInvocation methods,
    // which no interceptor sees. With concurrent execution, an invocation can start on a thread that QuickPerf has not
    // marked yet: this callback, registered on the class, is called before it.
    @Test
    void should_mark_the_thread_starting_an_invocation_of_a_parameterized_class() throws InterruptedException {

        // WHEN
        Boolean threadMarked = runOnANewThread(() -> {
            new QuickPerfTestExtension().beforeClassTemplateInvocation(null);
            return TestThreads.isCurrentThreadMarked();
        });

        // THEN
        assertThat(threadMarked).as("thread marked as a test thread").isTrue();

    }

    private interface ThreadTask<T> {
        T execute();
    }

    private static <T> T runOnANewThread(ThreadTask<T> task) throws InterruptedException {
        AtomicReference<T> result = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean();
        Thread thread = new Thread(() -> {
            result.set(task.execute());
            completed.set(true);
        });
        thread.start();
        thread.join();
        assertThat(completed).as("task completed").isTrue();
        return result.get();
    }

}
