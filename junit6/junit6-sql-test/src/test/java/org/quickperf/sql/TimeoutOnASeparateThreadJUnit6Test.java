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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.Selects.executeASelect;

// With @Timeout(threadMode = SEPARATE_THREAD), JUnit executes the test method on a thread started for it, around the
// QuickPerf interceptor, while QuickPerf builds the context in beforeEach, on the test thread
class TimeoutOnASeparateThreadJUnit6Test {

    @QuickPerfTest
    static class TestMethodOnASeparateThread {

        static final AtomicReference<Thread> BEFORE_EACH_THREAD = new AtomicReference<>();

        @BeforeEach
        void before_each() {
            BEFORE_EACH_THREAD.set(Thread.currentThread());
        }

        @DisplaySql
        @ExpectSelect(1)
        @Timeout(value = 1, unit = TimeUnit.MINUTES, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
        @Test
        void execute_a_select() {
            assertThat(Thread.currentThread()).as("thread of the test method")
                                              .isNotSameAs(BEFORE_EACH_THREAD.get());
            executeASelect();
        }

    }

    @Test
    void should_record_the_sql_of_a_test_method_executed_on_a_separate_thread() {

        // WHEN: on this thread, as the beforeEach callbacks
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestMethodOnASeparateThread.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isOne();
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread that built the context")
                .isNull();

    }

}
