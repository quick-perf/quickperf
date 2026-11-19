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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.experimental.results.PrintableResult;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.perfrecording.TestThreads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

// QuickPerf marks the threads executing the code of a test class as test
// threads, even if QuickPerf is disabled for the test: their SQL is recorded
// for their own test only, and not for the tests recording at the same time.
// JUnit may execute this code on another thread than the one executing the
// test method.
public class ThreadsExecutingTestCodeTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassLevelCode {

        @ClassRule
        public static final TestRule CLASS_RULE = new ExternalResource() {
            @Override
            protected void before() {
                assertIsATestThread("class rule");
            }
        };

        @BeforeClass
        public static void before_class() {
            assertIsATestThread("@BeforeClass method");
        }

        @Test
        public void a_test() {
        }

        @AfterClass
        public static void after_class() {
            assertIsATestThread("@AfterClass method");
        }

    }

    @Test public void
    should_mark_the_thread_executing_the_class_level_code() throws Exception {

        // WHEN
        PrintableResult printableResult = executeOnANewThread(ClassLevelCode.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TestInstanceCreation {

        public TestInstanceCreation() {
            assertIsATestThread("constructor");
        }

        @Test
        public void a_test() {
        }

        @DisableQuickPerf
        @Test
        public void a_test_with_quickperf_disabled() {
        }

    }

    @Test public void
    should_mark_the_thread_creating_the_test_instance() {

        // WHEN: the test methods on other threads than the class
        Result result = JUnitCore.runClasses(new ParallelComputer(false, true), TestInstanceCreation.class);

        // THEN
        assertThat(result.getFailures()).isEmpty();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TimeoutRule {

        @Rule
        public Timeout timeout = Timeout.seconds(20);

        @Before
        public void before() {
            assertIsATestThread("@Before method");
        }

        @Test
        public void a_test() {
            assertIsATestThread("test method");
        }

        @DisableQuickPerf
        @Test
        public void a_test_with_quickperf_disabled() {
            assertIsATestThread("test method with QuickPerf disabled");
        }

        @After
        public void after() {
            assertIsATestThread("@After method");
        }

    }

    @Test public void
    should_mark_the_thread_of_a_timeout_rule() throws Exception {

        // WHEN
        PrintableResult printableResult = executeOnANewThread(TimeoutRule.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TestWithATimeout {

        @Test(timeout = 20_000)
        public void a_test() {
            assertIsATestThread("test method");
        }

        @DisableQuickPerf
        @Test(timeout = 20_000)
        public void a_test_with_quickperf_disabled() {
            assertIsATestThread("test method with QuickPerf disabled");
        }

    }

    @Test public void
    should_mark_the_thread_executing_a_test_method_with_a_timeout() throws Exception {

        // WHEN
        PrintableResult printableResult = executeOnANewThread(TestWithATimeout.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    // On a thread that executed no test yet
    private static PrintableResult executeOnANewThread(final Class<?> testClass) throws Exception {
        ExecutorService newThread = Executors.newSingleThreadExecutor();
        try {
            return newThread.submit(() -> testResult(testClass)).get(60, TimeUnit.SECONDS);
        } finally {
            newThread.shutdownNow();
        }
    }

    private static void assertIsATestThread(String testCode) {
        assertThat(TestThreads.isCurrentThreadMarked())
                .as("test thread executing the " + testCode + ": " + Thread.currentThread().getName())
                .isTrue();
    }

}
