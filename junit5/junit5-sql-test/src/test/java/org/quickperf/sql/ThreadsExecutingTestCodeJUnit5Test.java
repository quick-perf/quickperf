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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.perfrecording.TestThreads;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

// QuickPerf marks the threads executing the code of a test class as test
// threads, even if QuickPerf is disabled for the test: their SQL is recorded
// for their own test only, and not for the tests recording at the same time.
// JUnit may execute this code on several threads: with concurrent execution,
// or with @Timeout(threadMode = SEPARATE_THREAD) (JUnit 5.9+), which executes
// each lifecycle method and test method on a thread started for it.
// OnNewThreads does the same with the JUnit version of this module. It must be
// declared before @QuickPerfTest to be executed first.
public class ThreadsExecutingTestCodeJUnit5Test {

    public static class OnNewThreads implements InvocationInterceptor {

        @Override
        public <T> T interceptTestClassConstructor( Invocation<T> invocation
                                                  , ReflectiveInvocationContext<Constructor<T>> invocationContext
                                                  , ExtensionContext extensionContext) throws Throwable {
            return proceedOnANewThread(invocation);
        }

        @Override
        public void interceptBeforeAllMethod( Invocation<Void> invocation
                                            , ReflectiveInvocationContext<Method> invocationContext
                                            , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public void interceptBeforeEachMethod( Invocation<Void> invocation
                                             , ReflectiveInvocationContext<Method> invocationContext
                                             , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public void interceptTestMethod( Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public void interceptTestTemplateMethod( Invocation<Void> invocation
                                               , ReflectiveInvocationContext<Method> invocationContext
                                               , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public <T> T interceptTestFactoryMethod( Invocation<T> invocation
                                               , ReflectiveInvocationContext<Method> invocationContext
                                               , ExtensionContext extensionContext) throws Throwable {
            return proceedOnANewThread(invocation);
        }

        @Override
        public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public void interceptAfterEachMethod( Invocation<Void> invocation
                                            , ReflectiveInvocationContext<Method> invocationContext
                                            , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        @Override
        public void interceptAfterAllMethod( Invocation<Void> invocation
                                           , ReflectiveInvocationContext<Method> invocationContext
                                           , ExtensionContext extensionContext) throws Throwable {
            proceedOnANewThread(invocation);
        }

        private static <T> T proceedOnANewThread(Invocation<T> invocation) throws Throwable {
            AtomicReference<T> result = new AtomicReference<>();
            AtomicReference<Throwable> throwable = new AtomicReference<>();
            Thread newThread = new Thread(() -> {
                try {
                    result.set(invocation.proceed());
                } catch (Throwable invocationThrowable) {
                    throwable.set(invocationThrowable);
                }
            }, "new-thread");
            newThread.start();
            newThread.join();
            if (throwable.get() != null) {
                throw throwable.get();
            }
            return result.get();
        }

    }

    @ExtendWith(OnNewThreads.class)
    @QuickPerfTest
    static class TestCodeOnNewThreads {

        TestCodeOnNewThreads() {
            assertIsATestThread("constructor");
        }

        @BeforeAll
        static void before_all() {
            assertIsATestThread("@BeforeAll method");
        }

        @BeforeEach
        void before_each() {
            assertIsATestThread("@BeforeEach method");
        }

        @Test
        void a_test() {
            assertIsATestThread("test method");
        }

        @DisableQuickPerf
        @Test
        void a_test_with_quickperf_disabled() {
            assertIsATestThread("test method with QuickPerf disabled");
        }

        @RepeatedTest(2)
        void a_test_template() {
            assertIsATestThread("test template method");
        }

        @TestFactory
        List<DynamicTest> a_test_factory() {
            assertIsATestThread("test factory method");
            return Collections.singletonList(dynamicTest("a dynamic test", () -> assertIsATestThread("dynamic test")));
        }

        @AfterEach
        void after_each() {
            assertIsATestThread("@AfterEach method");
        }

        @AfterAll
        static void after_all() {
            assertIsATestThread("@AfterAll method");
        }

    }

    @Test
    void should_mark_the_threads_executing_the_code_of_a_test_class() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestCodeOnNewThreads.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(5);

    }

    // The callbacks of the extensions registered after QuickPerf are executed
    // after those of QuickPerf, before the lifecycle methods
    public static class ExtensionRegisteredAfterQuickPerf implements BeforeAllCallback, BeforeEachCallback {

        @Override
        public void beforeAll(ExtensionContext extensionContext) {
            assertIsATestThread("beforeAll callback");
        }

        @Override
        public void beforeEach(ExtensionContext extensionContext) {
            assertIsATestThread("beforeEach callback");
        }

    }

    @QuickPerfTest
    @ExtendWith(ExtensionRegisteredAfterQuickPerf.class)
    static class CallbacksOfAnotherExtension {

        @Test
        void a_test() {
        }

    }

    @Test
    void should_mark_the_thread_executing_the_class_before_the_callbacks_of_the_other_extensions() throws Exception {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(CallbacksOfAnotherExtension.class);

        // WHEN: on a thread that executed no test yet
        ExecutorService newThread = Executors.newSingleThreadExecutor();
        JUnit5TestsResult result;
        try {
            result = newThread.submit(jUnit5Tests::run).get(60, TimeUnit.SECONDS);
        } finally {
            newThread.shutdownNow();
        }

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isOne();

    }

    // With concurrent execution, the tests of a class can be executed on
    // other threads than the class. With a single test instance, these
    // threads create no test instance. JUnit executes the tests of such a
    // class concurrently only if the class declares it.
    @QuickPerfTest
    @ExtendWith(ExtensionRegisteredAfterQuickPerf.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Execution(ExecutionMode.CONCURRENT)
    static class CallbacksOfAnotherExtensionOnConcurrentThreads {

        private final CountDownLatch testsExecuting = new CountDownLatch(2);

        @Test
        void a_test() throws InterruptedException {
            awaitTheOtherTest();
        }

        @Test
        void another_test() throws InterruptedException {
            awaitTheOtherTest();
        }

        private void awaitTheOtherTest() throws InterruptedException {
            testsExecuting.countDown();
            assertThat(testsExecuting.await(10, TimeUnit.SECONDS)).as("tests executed concurrently").isTrue();
        }

    }

    @Test
    void should_mark_the_threads_executing_the_tests_before_the_callbacks_of_the_other_extensions() {

        // GIVEN
        Map<String, String> concurrentExecution = new HashMap<>();
        concurrentExecution.put("junit.jupiter.execution.parallel.enabled", "true");
        concurrentExecution.put("junit.jupiter.execution.parallel.mode.default", "concurrent");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.strategy", "fixed");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.fixed.parallelism", "2");
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(CallbacksOfAnotherExtensionOnConcurrentThreads.class, concurrentExecution);

        // WHEN
        JUnit5TestsResult result = jUnit5Tests.run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(2);

    }

    private static void assertIsATestThread(String testCode) {
        assertThat(TestThreads.isCurrentThreadMarked())
                .as("test thread executing the " + testCode + ": " + Thread.currentThread().getName())
                .isTrue();
    }

}
