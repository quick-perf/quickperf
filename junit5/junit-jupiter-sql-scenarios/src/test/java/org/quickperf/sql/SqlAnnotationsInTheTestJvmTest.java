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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.writer.WriterFactory;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// SQL annotations executed in the test JVM
class SqlAnnotationsInTheTestJvmTest {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    private static final String SQL_OF_THE_BEFORE_EACH_METHOD = "select 'sql-of-the-before-each-method'";

    private static final String SQL_OF_THE_TEST_METHOD = "select 'sql-of-the-test-method'";

    @QuickPerfUnderTest
    static class DisplayingTheSqlOfTheTestMethodBody {

        @BeforeEach
        public void before_each() {
            executeSql(SQL_OF_THE_BEFORE_EACH_METHOD);
        }

        @DisplaySqlOfTestMethodBody
        @Test
        public void a_test() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

    }

    @Test
    public void should_display_the_sql_of_the_test_method_body_only() {

        // WHEN
        String standardOutput = standardOutputOf(DisplayingTheSqlOfTheTestMethodBody.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-test-method")
                                  .doesNotContain("sql-of-the-before-each-method");

    }

    @QuickPerfUnderTest
    static class DisplayingTheSql {

        @BeforeEach
        public void before_each() {
            executeSql(SQL_OF_THE_BEFORE_EACH_METHOD);
        }

        @DisplaySql
        @Test
        public void a_test() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

    }

    @Test
    public void should_display_the_sql_of_the_before_each_methods_and_of_the_test_method() {

        // WHEN
        String standardOutput = standardOutputOf(DisplayingTheSql.class);

        // THEN
        assertThat(standardOutput).contains("sql-of-the-before-each-method")
                                  .contains("sql-of-the-test-method");

    }

    @QuickPerfUnderTest
    static class LeakingAConnection {

        private Connection connection;

        @ExpectNoConnectionLeak
        @Test
        public void a_test_not_closing_its_connection() throws SQLException {
            connection = DATA_SOURCE.getConnection();
        }

        // After the check of QuickPerf
        @AfterEach
        public void close_the_connection() throws SQLException {
            if (connection != null) {
                connection.close();
            }
        }

    }

    @Test
    public void should_fail_a_test_leaking_a_connection() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(LeakingAConnection.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains("Database connection leak");

    }

    public static class AnalysisWriterFactory implements WriterFactory {

        static final StringWriter ANALYSIS = new StringWriter();

        @Override
        public Writer buildWriter() {
            return ANALYSIS;
        }

    }

    @QuickPerfUnderTest
    static class AnalyzingTheSql {

        @AnalyzeSql(writerFactory = AnalysisWriterFactory.class)
        @Test
        public void execute_one_select() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

    }

    @Test
    public void should_analyze_the_sql_of_the_test_method() {

        // GIVEN
        AnalysisWriterFactory.ANALYSIS.getBuffer().setLength(0);

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(AnalyzingTheSql.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(AnalysisWriterFactory.ANALYSIS.toString()).contains("[QUICK PERF] SQL ANALYSIS")
                                                             .contains("SQL EXECUTIONS: 1")
                                                             .contains("SELECT: 1");

    }

    @QuickPerfUnderTest
    @ExpectSelect(2)
    static class SqlAnnotationOnTheClass {

        @Test
        public void execute_one_select() {
            executeSql(SQL_OF_THE_TEST_METHOD);
        }

    }

    @Test
    public void should_apply_a_sql_annotation_of_the_test_class() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(SqlAnnotationOnTheClass.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains("You may think that <2> select statements were sent to the database")
                                           .contains("But there is in fact <1>...");

    }

    @QuickPerfUnderTest
    static class ParameterizedTestExecutingSelects {

        @ExpectSelect(1)
        @ParameterizedTest
        @ValueSource(ints = {1, 2})
        public void execute_selects(int numberOfSelects) {
            for (int i = 0; i < numberOfSelects; i++) {
                executeSql(SQL_OF_THE_TEST_METHOD);
            }
        }

    }

    @Test
    public void should_check_each_invocation_of_a_parameterized_test() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(ParameterizedTestExecutingSelects.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isOne();
        assertThat(result.getNumberOfSucceededTests()).isOne();
        assertThat(result.getErrorReport()).contains("You may think that <1> select statement")
                                           .contains("But there are in fact <2>...");

    }

    // As the annotations of a superclass, the annotations of the enclosing class do not apply to the tests of a nested
    // class: only the annotations of the test method and of its declaring class do
    @QuickPerfUnderTest
    @ExpectSelect(5)
    static class EnclosingClassWithASqlAnnotation {

        @Nested
        class NestedTests {

            @Test
            public void execute_one_select_without_annotation() {
                executeSql(SQL_OF_THE_TEST_METHOD);
            }

            @ExpectSelect(1)
            @Test
            public void execute_one_select_expecting_one() {
                executeSql(SQL_OF_THE_TEST_METHOD);
            }

            @ExpectSelect(2)
            @Test
            public void execute_one_select_expecting_two() {
                executeSql(SQL_OF_THE_TEST_METHOD);
            }

        }

    }

    @Test
    public void should_check_the_tests_of_a_nested_class_with_their_own_annotations() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(EnclosingClassWithASqlAnnotation.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).as(errorReport).isOne();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(2);
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                               .doesNotContain("<5>");

    }

    private static String standardOutputOf(Class<?> testClass) {
        PrintStream standardOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        AtomicReference<JUnit5TestsResult> result = new AtomicReference<>();
        try {
            System.setOut(new PrintStream(output, true, "UTF-8"));
            result.set(JUnit5Tests.createInstance(testClass).run());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } finally {
            System.setOut(standardOutput);
        }
        assertThat(result.get().getNumberOfFailures()).as(result.get().getErrorReport()).isZero();
        try {
            return output.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void executeSql(String sql) {
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery(sql).close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
