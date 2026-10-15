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
import org.quickperf.sql.annotation.AllowStatements;
import org.quickperf.sql.annotation.DisableStatements;
import org.quickperf.sql.annotation.EnableStatements;
import org.quickperf.sql.annotation.ExpectNoStatements;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

@SuppressWarnings("deprecation")
public class ExpectNoStatementsTest {

    private static final String STATEMENT_ISSUE =
            "At least one Statement. Only PreparedStatement and CallableStatement were expected.";

    @RunWith(QuickPerfJUnitRunner.class)
    public static class PreparedStatementAndExpectNoStatements extends SqlTestBase {

        @ExpectNoStatements
        @Test
        public void execute_one_select_with_a_prepared_statement() throws SQLException {
            executeOneSelectWithAPreparedStatement(getConnection());
        }

    }

    @Test public void
    should_pass_with_prepared_statement() {

        // GIVEN
        Class<?> testClass = PreparedStatementAndExpectNoStatements.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementAndExpectNoStatements extends SqlTestBase {

        @ExpectNoStatements
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    should_fail_with_statement() {

        // GIVEN
        Class<?> testClass = StatementAndExpectNoStatements.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).contains(STATEMENT_ISSUE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class PreparedStatementAndExpectNoStatementsInADedicatedJVM extends SqlTestBase {

        @ExpectNoStatements
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_a_prepared_statement() throws SQLException {
            executeOneSelectWithAPreparedStatement(getConnection());
        }

    }

    @Test public void
    should_pass_with_prepared_statement_and_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = PreparedStatementAndExpectNoStatementsInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementAndExpectNoStatementsInADedicatedJVM extends SqlTestBase {

        @ExpectNoStatements
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    should_fail_with_statement_in_a_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = StatementAndExpectNoStatementsInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).contains(STATEMENT_ISSUE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoStatements
    public static class StatementsAllowedOnMethod extends SqlTestBase {

        @AllowStatements(comment = "Statements are allowed here")
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    should_pass_if_statements_are_allowed() {

        // GIVEN
        Class<?> testClass = StatementsAllowedOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableStatements
    public static class DeprecatedAnnotationOnClassAndStatementsAllowedOnMethod extends SqlTestBase {

        @AllowStatements
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    allow_statements_should_cancel_the_deprecated_disable_statements() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndStatementsAllowedOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoStatements
    public static class DeprecatedEnableAnnotationOnMethod extends SqlTestBase {

        @EnableStatements
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    the_deprecated_enable_statements_should_cancel_expect_no_statements() {

        // GIVEN
        Class<?> testClass = DeprecatedEnableAnnotationOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableStatements
    public static class DeprecatedAnnotationOnClassAndNewAnnotationOnMethod extends SqlTestBase {

        @ExpectNoStatements
        @Test
        public void execute_one_select_with_a_statement() throws SQLException {
            executeOneSelectWithAStatement(getConnection());
        }

    }

    @Test public void
    should_report_statements_once_if_the_deprecated_and_the_new_annotations_are_both_applied() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndNewAnnotationOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).containsOnlyOnce(STATEMENT_ISSUE);

    }

    private static void executeOneSelectWithAPreparedStatement(Connection connection) throws SQLException {
        try (Connection autoClosedConnection = connection;
             PreparedStatement statement = autoClosedConnection.prepareStatement("select isbn from Book")) {
            statement.executeQuery();
        }
    }

    private static void executeOneSelectWithAStatement(Connection connection) throws SQLException {
        try (Connection autoClosedConnection = connection;
             Statement statement = autoClosedConnection.createStatement()) {
            statement.executeQuery("select * from Book");
        }
    }

}
