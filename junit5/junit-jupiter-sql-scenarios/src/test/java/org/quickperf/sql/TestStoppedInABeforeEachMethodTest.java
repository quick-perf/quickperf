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
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.sql.display.DisplaySqlRecorder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// The recorder of @DisplaySql registers itself when QuickPerf builds the context, in beforeEach. When a @BeforeEach
// method stops the test, the test method is not executed: QuickPerf releases the recorder in afterEach. Otherwise, the
// recorder would display the SQL of the next tests executed by the thread.
class TestStoppedInABeforeEachMethodTest {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    @QuickPerfUnderTest
    static class TestAbortedInABeforeEachMethod {

        @BeforeEach
        void before_each() {
            assumeTrue(false, "Assumption failing in a @BeforeEach method");
        }

        @DisplaySql
        @ExpectSelect(1)
        @Test
        void execute_a_select() {
            executeASelect();
        }

    }

    @Test
    void should_release_the_recorders_of_a_test_aborted_in_a_before_each_method() {

        // WHEN: on this thread, as the beforeEach callbacks
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestAbortedInABeforeEachMethod.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfAbortedTests()).isOne();
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread that built the context")
                .isNull();

    }

    @QuickPerfUnderTest
    static class TestFailingInABeforeEachMethod {

        @BeforeEach
        void before_each() {
            throw new IllegalStateException("Failure of a @BeforeEach method");
        }

        @DisplaySql
        @ExpectSelect(1)
        @Test
        void execute_a_select() {
            executeASelect();
        }

    }

    @Test
    void should_release_the_recorders_of_a_test_failing_in_a_before_each_method() {

        // WHEN: on this thread, as the beforeEach callbacks
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestFailingInABeforeEachMethod.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(errorReport).contains("Failure of a @BeforeEach method")
                               .doesNotContain("QUICK PERF");
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread that built the context")
                .isNull();

    }

    private static void executeASelect() {
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("select 1").close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
