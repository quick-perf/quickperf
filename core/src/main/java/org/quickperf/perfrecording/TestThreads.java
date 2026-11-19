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
package org.quickperf.perfrecording;

/**
 * The threads executing tests. The test runners mark the threads executing the
 * code of a test class, even if QuickPerf is disabled for the test: the
 * class-level setup and teardown, the creation of the test instance, the setup
 * and teardown of each test, and the test method, including on a thread
 * started by a timeout. The SQL executed by a marked thread is recorded for its
 * own test only, and not recorded if its test is not recording. The SQL
 * executed by another thread, such as an HTTP worker or an executor thread, is
 * recorded for every test recording at that time.
 */
public final class TestThreads {

    // Not inheritable: a thread started by a test is not a test thread.
    // Released when its thread ends.
    private static final ThreadLocal<Boolean> TEST_THREAD = new ThreadLocal<Boolean>();

    private TestThreads() { }

    public static void markCurrentThread() {
        TEST_THREAD.set(Boolean.TRUE);
    }

    public static boolean isCurrentThreadMarked() {
        return TEST_THREAD.get() != null;
    }

}
