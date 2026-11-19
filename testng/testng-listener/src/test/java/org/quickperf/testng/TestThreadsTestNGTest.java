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
package org.quickperf.testng;

import org.quickperf.testng.TestNGTests.TestNGTestsResult;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

// QuickPerf marks the threads executing the code of a test class as test
// threads, even if QuickPerf is disabled for the test: their SQL is recorded
// for their own test only, and not for the tests recording at the same time.
public class TestThreadsTestNGTest {

    @Test public void
    should_mark_the_threads_executing_the_code_of_a_test_class() throws Exception {

        // GIVEN
        final TestNGTests testNGTests = TestNGTests.createInstance(TestNGCodeOnSeveralThreads.class);

        // WHEN
        TestNGTestsResult testsResult = executeOnANewThread(new Callable<TestNGTestsResult>() {
            @Override
            public TestNGTestsResult call() {
                return testNGTests.run();
            }
        });

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isZero();
        assertThat(testsResult.getNumberOfPassedTest()).isEqualTo(3);

    }

    // On a thread that executed no test yet
    private static <T> T executeOnANewThread(Callable<T> callable) throws Exception {
        ExecutorService newThread = Executors.newSingleThreadExecutor();
        try {
            return newThread.submit(callable).get(60, TimeUnit.SECONDS);
        } finally {
            newThread.shutdownNow();
        }
    }

}
