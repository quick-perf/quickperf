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
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.annotation.DisableStatements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableStatementsTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class PreparedStatementAndDisableStatementsAnnotation extends SqlTestBase {

        @DisableStatements
        @Test
        public void execute_one_select_with_a_prepared_statement() {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book")) {
                statement.executeQuery();
            } catch (SQLException sqlException) {
                throw new IllegalStateException(sqlException);
            }
        }

    }

    @Test public void
    should_pass_with_prepared_statement() {

        // GIVEN
        Class<?> testClass = PreparedStatementAndDisableStatementsAnnotation.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementAndDisableStatementsAnnotation extends SqlTestBase {

        @DisableStatements
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("select * from Book");
            }
        }

    }

    @Test public void
    should_fail_with_statement() {

        // GIVEN
        Class<?> testClass = StatementAndDisableStatementsAnnotation.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        String testReport = testResult.toString();
        assertThat(testReport).contains(
                "At least one Statement. Only PreparedStatement and CallableStatement were expected.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class PreparedStatementAndDisableStatementsAnnotationInADedicatedJVM extends SqlTestBase {

        @DisableStatements
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_a_prepared_statement() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book")) {
                statement.executeQuery();
            }
        }

    }

    @Test public void
    should_pass_with_prepared_statement_and_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = PreparedStatementAndDisableStatementsAnnotationInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementAndDisableStatementsAnnotationInADedicatedJVM extends SqlTestBase {

        @DisableStatements
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("select * from Book");
            }
        }

    }

    @Test public void
    should_fail_with_statement_in_a_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = StatementAndDisableStatementsAnnotationInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        String testReport = testResult.toString();
        assertThat(testReport).contains(
                "At least one Statement. Only PreparedStatement and CallableStatement were expected.");

    }

}
