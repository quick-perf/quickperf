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
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.AnalyzeSql;
import org.quickperf.sql.annotation.DisableLikeWithLeadingWildcard;
import org.quickperf.writer.WriterFactory;

import javax.persistence.Query;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.lang.System.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class AnalyzeSqlTest {

    private static final String SELECT_FILE_PATH = findTargetPath() + File.separator + "select-result.txt";
    private static final String INSERT_FILE_PATH = findTargetPath() + File.separator + "insert-result.txt";
    private static final String UPDATE_FILE_PATH = findTargetPath() + File.separator + "update-result.txt";
    private static final String DELETE_FILE_PATH = findTargetPath() + File.separator + "delete-result.txt";
    private static final String NOTHING_HAPPENED = findTargetPath() + File.separator + "no-result.txt";
    private static final String MULTIPLE_EXECUTIONS = findTargetPath() + File.separator + "sql-executions.txt";
    private static final String SELECT_SPECIFIC_MESSAGES = findTargetPath() + File.separator + "select-specific.txt";
    private static final String DETECT_N_PLUS_ONE = findTargetPath() + File.separator + "n-plus-one.txt";
    private static final String WILDCARD_SELECT = findTargetPath() + File.separator + "wildcard-select.txt";
    private static final String BIND_PARAMETERS = findTargetPath() + File.separator + "bind-parameters.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    private static String getFileContent(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                    .collect(Collectors.joining(lineSeparator()));
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoExecution extends SqlTestBase {
        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(NOTHING_HAPPENED);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void noExecution() {
        }

    }

    @Test
    public void should_report_nothing() throws IOException {

        // GIVEN
        Class<?> classUnderTest = NoExecution.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        assertThat(getFileContent(NOTHING_HAPPENED))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 0");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectExecution extends SqlTestBase {
        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(SELECT_FILE_PATH);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void select() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();
            });
        }

    }

    @Test
    public void should_report_select() throws IOException {

        // GIVEN
        Class<?> classUnderTest = SelectExecution.class;

        // WHEN
        PrintableResult testResult = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        assertThat(getFileContent(SELECT_FILE_PATH))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 1")
                .contains("SELECT: 1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(INSERT_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void insert() {
            executeInATransaction(entityManager -> {
                Book effectiveJava = new Book();
                effectiveJava.setIsbn("effectiveJavaIsbn");
                effectiveJava.setTitle("Effective Java");
                entityManager.persist(effectiveJava);
            });
        }

    }

    @Test
    public void should_report_insert() throws IOException {
        // GIVEN
        Class<?> classUnderTest = InsertExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(INSERT_FILE_PATH))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 2") // Hibernate sequence call is also called
                .contains("INSERT: 1");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class UpdateExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(UPDATE_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void update() {
            executeInATransaction(entityManager -> {
                String sql = " UPDATE book"
                        + " SET isbn ='978-0134685991'"
                        + " WHERE id = 1";
                Query query = entityManager.createNativeQuery(sql);
                query.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_update() throws IOException {
        // GIVEN
        Class<?> classUnderTest = UpdateExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(UPDATE_FILE_PATH))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 1")
                .contains("UPDATE: 1");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DeleteExecution extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(DELETE_FILE_PATH);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void delete() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("DELETE FROM " + Book.class.getCanonicalName());
                query.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_delete() throws IOException {
        // GIVEN
        Class<?> classUnderTest = DeleteExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(DELETE_FILE_PATH))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 1")
                .contains("DELETE: 1");
    }

    @Test
    public void should_display_method_body() throws IOException {
        // GIVEN
        Class<?> classUnderTest = DeleteExecution.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(DELETE_FILE_PATH))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 1")
                .contains("DELETE: 1")
                .contains("delete")
                .contains("from")
                .contains("Book");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlExecutions_are_properly_analyzed extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(MULTIPLE_EXECUTIONS);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void queries() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();

                String insertTwo = "INSERT INTO Book (id,title) VALUES (1300, 'Book title')";
                Query secondInsertQuery = entityManager.createNativeQuery(insertTwo);
                secondInsertQuery.executeUpdate();
            });
        }

    }

    @Test
    public void should_report_sql_executions() throws IOException {
        // GIVEN
        Class<?> classUnderTest = SqlExecutions_are_properly_analyzed.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(MULTIPLE_EXECUTIONS))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 2")
                .contains("SELECT: 1")
                .contains("INSERT: 1");
    }

    @Test
    public void should_display_max_query_execution_time() throws IOException {
        // GIVEN
        Class<?> classUnderTest = SqlExecutions_are_properly_analyzed.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(MULTIPLE_EXECUTIONS))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 2")
                .contains("SELECT: 1")
                .contains("INSERT: 1")
                .contains("MAX TIME:")
                .contains("ms");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectIssues extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(SELECT_SPECIFIC_MESSAGES);
            }
        }

        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        @Test
        public void queries() {
            executeInATransaction(entityManager -> {
                Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                query.getResultList();

                Query sameQueryAgain = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
                sameQueryAgain.getResultList();

            });
        }

    }

    @Test
    public void should_report_same_selects() throws IOException {
        // GIVEN
        Class<?> classUnderTest = SelectIssues.class;

        // WHEN
        PrintableResult result = PrintableResult.testResult(classUnderTest);

        // THEN
        assertThat(result.failureCount()).isZero();

        assertThat(getFileContent(SELECT_SPECIFIC_MESSAGES))
                .contains("[QUICK PERF] SQL ANALYSIS")
                .contains("SQL EXECUTIONS: 2")
                .contains("SELECT: 2")
                .contains("Same SELECT statements");
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NplusOneIssues extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(DETECT_N_PLUS_ONE);
            }
        }

        @AnalyzeSql(writerFactory = NplusOneIssues.FileWriterBuilder.class)
        @Test
        public void execute_two_same_select_types_with_two_diff_param_values() {

            executeInATransaction(entityManager -> {
                String paramName = "idParam";
                String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id=:" + paramName;

                Query query = entityManager.createQuery(hqlQuery);
                query.setParameter(paramName, 2L);
                query.getResultList();

                Query query2 = entityManager.createQuery(hqlQuery);
                query2.setParameter(paramName, 1L);
                query2.getResultList();

            });
        }

    }

    @Test
    public void
    should_alert_if_n_plus_one_issue_is_detected() throws IOException {

        // GIVEN
        Class<?> testClass = NplusOneIssues.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
        assertThat(getFileContent(DETECT_N_PLUS_ONE))
                .contains("N+1")
                // The JDBC roundtrip message has to be displayed after the N+1 select message
                // , just before displaying the queries in this test case.
                .contains("You may have even more select statements with production data." + lineSeparator()
                        + "Be careful with the cost of JDBC roundtrips: https://blog.jooq.org/2017/12/18/the-cost-of-jdbc-server-roundtrips/"
                        + lineSeparator()
                        + lineSeparator()
                        + "                                            * * * * *"
                        + lineSeparator()
                        + "QUERIES"
                    );

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableLikeWithLeadingWildcard
    public static class SelectWithWildcard extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(WILDCARD_SELECT);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = SelectWithWildcard.FileWriterBuilder.class)
        public void execute_select_who_started_with_like_wildcard() {
            executeInATransaction(entityManager -> {
                Query nativeQuery = entityManager.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE  '%Ja'");
                nativeQuery.getResultList();
            });
        }

    }

    @Test
    public void
    should_alert_if_select_containing_like_with_percentage_leading_wildcard() throws IOException {

        // GIVEN
        Class<?> testClass = SelectWithWildcard.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(getFileContent(WILDCARD_SELECT))
                .contains("Like with leading wildcard detected (% or _)");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertWithoutBindParameters extends SqlTestBase {

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(BIND_PARAMETERS);
            }
        }

        @Test
        @AnalyzeSql(writerFactory = FileWriterBuilder.class)
        public void insert_without_bind_parameters() {
            executeInATransaction(entityManager -> {
                String sql = "INSERT INTO book VALUES (50L, 'CLEAN CODE','978-03213566974s')";
                Query nativeQuery = entityManager.createNativeQuery(sql);
                nativeQuery.executeUpdate();
            });
        }

    }

    @Test
    public void should_alert_if_no_bind_parameters_where_found() throws IOException {

        // GIVEN
        Class<?> testClass = InsertWithoutBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
        assertThat(getFileContent(BIND_PARAMETERS))
                .contains("Query without bind parameters");

    }

}
