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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.quickperf.sql.Selects.executeSql;

// @DisplaySql displays the SQL executed from the @BeforeEach methods to the @AfterEach methods, also when JUnit
// executes them on threads started for them. Its recorder registers itself when QuickPerf builds the context, and is
// released once the test is over.
class DisplaySqlJUnit6Test {

    private static final String SQL_OF_THE_BEFORE_EACH_METHOD = "select 'sql-of-the-before-each-method'";

    private static final String SQL_OF_THE_TEST_METHOD = "select 'sql-of-the-test-method'";

    private static final String SQL_OF_THE_AFTER_EACH_METHOD = "select 'sql-of-the-after-each-method'";

    private static final String SQL_OF_AN_AFTER_EACH_CALLBACK = "select 'sql-of-an-after-each-callback'";

    // Registered after QuickPerf: JUnit calls its afterEach callback before the one of QuickPerf, as for the rollback of
    // the test transaction by SpringExtension
    static class ExecutingSqlAfterEachTest implements AfterEachCallback {

        @Override
        public void afterEach(ExtensionContext context) {
            executeSql(SQL_OF_AN_AFTER_EACH_CALLBACK);
        }

    }

    @QuickPerfTest
    static class DisplayingTheSqlOfTheLifecycleMethods {

        @BeforeEach
        void before_each() {
            executeSql(SQL_OF_THE_BEFORE_EACH_METHOD);
        }

        @DisplaySql
        @Test
        void a_test() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

        @AfterEach
        void after_each() {
            executeSql(SQL_OF_THE_AFTER_EACH_METHOD);
        }

    }

    @Test
    void should_display_the_sql_of_the_before_each_and_after_each_methods() {

        // WHEN: on this thread, as the test
        String standardOutput = standardOutputOf(DisplayingTheSqlOfTheLifecycleMethods.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-before-each-method")
                                  .contains("sql-of-the-test-method")
                                  .contains("sql-of-the-after-each-method");
        assertThatTheRecorderOfDisplaySqlIsReleased();

    }

    // No @AfterEach method: its interceptor would bind the recorder to the thread again
    @QuickPerfTest
    @ExtendWith(ExecutingSqlAfterEachTest.class)
    static class DisplayingTheSqlOfAnAfterEachCallback {

        @DisplaySql
        @Test
        void a_test() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

    }

    @Test
    void should_display_the_sql_of_the_after_each_callbacks_of_the_extensions_registered_after_quickperf() {

        // WHEN
        String standardOutput = standardOutputOf(DisplayingTheSqlOfAnAfterEachCallback.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-test-method")
                                  .contains("sql-of-an-after-each-callback");
        assertThatTheRecorderOfDisplaySqlIsReleased();

    }

    @QuickPerfTest
    static class DisplayingTheSqlOfLifecycleMethodsExecutedOnSeparateThreads {

        @Timeout(value = 1, unit = TimeUnit.MINUTES, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
        @BeforeEach
        void before_each() {
            executeSql(SQL_OF_THE_BEFORE_EACH_METHOD);
        }

        @DisplaySql
        @Timeout(value = 1, unit = TimeUnit.MINUTES, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
        @Test
        void a_test() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

        @Timeout(value = 1, unit = TimeUnit.MINUTES, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
        @AfterEach
        void after_each() {
            executeSql(SQL_OF_THE_AFTER_EACH_METHOD);
        }

    }

    @Test
    void should_display_the_sql_of_the_lifecycle_methods_executed_on_separate_threads() {

        // WHEN
        String standardOutput = standardOutputOf(DisplayingTheSqlOfLifecycleMethodsExecutedOnSeparateThreads.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-before-each-method")
                                  .contains("sql-of-the-test-method")
                                  .contains("sql-of-the-after-each-method");
        assertThatTheRecorderOfDisplaySqlIsReleased();

    }

    @QuickPerfTest
    static class DynamicTestDisplayingItsSql {

        @DisplaySql
        @TestFactory
        DynamicTest a_test_factory() {
            return dynamicTest("a dynamic test", () -> executeSql(SQL_OF_THE_TEST_METHOD));
        }

    }

    // A dynamic test gets no afterEach callback
    @Test
    void should_display_the_sql_of_a_dynamic_test_and_release_its_recorder() {

        // WHEN
        String standardOutput = standardOutputOf(DynamicTestDisplayingItsSql.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-test-method");
        assertThatTheRecorderOfDisplaySqlIsReleased();

    }

    private static void assertThatTheRecorderOfDisplaySqlIsReleased() {
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread that built the context")
                .isNull();
    }

    private static String standardOutputOf(Class<?> testClass) {
        PrintStream standardOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AtomicReference<JUnit5TestsResult> result = new AtomicReference<>();
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            result.set(JUnit5Tests.createInstance(testClass).run());
        } finally {
            System.setOut(standardOutput);
        }
        assertThat(result.get().getNumberOfFailures()).as(result.get().getErrorReport()).isZero();
        assertThat(result.get().getNumberOfSucceededTests()).isOne();
        return output.toString(StandardCharsets.UTF_8);
    }

}
