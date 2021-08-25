/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.connection.Level;
import org.quickperf.writer.WriterFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ProfileConnectionTest {

    private static final String FILE_PATH = findTargetPath() + File.separator + "connection-profiling.txt";

    private static final String FILE_PATH_PROFILING_WITHOUT_STACK =  findTargetPath() + File.separator + "connection-profiling-without-stack.txt";

    private static final String FILE_PATH_STACK_DEPTH = findTargetPath() + File.separator + "connection-profiling-with-limited-stack.txt";

    private static final String FILE_PATH_STACK_WITHOUT_FILTERING = findTargetPath() + File.separator + "connection-profiling-stack-without-filtering.txt";

    private static final String FILE_PATH_PROFILING_BEFORE_AND_AFTER_TEST_METHOD = findTargetPath() + File.separator + "connection-profiling-before-after-test-method.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionClass extends SqlTestBase {
        @ProfileConnection(level = Level.TRACE
                         , displayStackTrace = true
                         , writerFactory = FileWriterBuilder.class
                          )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                 statement.executeQuery();
            }
        }
    }

    public static class FileWriterBuilder implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH);
        }
    }

    @Test public void
    should_profile_connection() throws IOException {

        // GIVEN
        Class<ProfileConnectionClass> testClass = ProfileConnectionClass.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        String profilingResult = readContentOf(FILE_PATH);
        assertThat(profilingResult.replaceAll("connection .* -", "connection id -")
                                  .replaceAll("java:.*\\)", "java:lineNumber\\)")
                  )
        .isEqualToNormalizingNewlines(
                        "connection id - javax.sql.DataSource.getConnection()\n" +
                        "\torg.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:lineNumber)\n" +
                        "\torg.hibernate.internal.NonContextualJdbcConnectionAccess.obtainConnection(NonContextualJdbcConnectionAccess.java:lineNumber)\n" +
                        "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded(LogicalConnectionManagedImpl.java:lineNumber)\n" +
                        "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.getPhysicalConnection(LogicalConnectionManagedImpl.java:lineNumber)\n" +
                        "\torg.hibernate.internal.SessionImpl.connection(SessionImpl.java:lineNumber)\n" +
                        "\tSqlTestBase.getConnection(SqlTestBase.java:lineNumber)\n" +
                        "\tProfileConnectionTest$ProfileConnectionClass.test(ProfileConnectionTest.java:lineNumber)\n" +
                        "connection id - java.sql.Connection.prepareStatement(String sql) [sql: select isbn from Book]\n" +
                        "\tProfileConnectionTest$ProfileConnectionClass.test(ProfileConnectionTest.java:lineNumber)\n" +
                        "connection id - java.sql.Connection.close()\n" +
                        "\tProfileConnectionTest$ProfileConnectionClass.test(ProfileConnectionTest.java:lineNumber)");

    }

    private String readContentOf(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                    .collect(joining(System.lineSeparator()));
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithoutStackClass extends SqlTestBase {
        @ProfileConnection( displayStackTrace = false
                          , writerFactory = FileWriterBuilderForProfilingWithoutStack.class
                          )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                statement.executeQuery();
            }
        }
    }

    public static class FileWriterBuilderForProfilingWithoutStack implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH_PROFILING_WITHOUT_STACK);
        }
    }

    @Test public void
    should_profile_connection_without_stack_trace() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionWithoutStackClass.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        String profilingResult = readContentOf(FILE_PATH_PROFILING_WITHOUT_STACK);
        assertThat(profilingResult).doesNotContain("org.hibernate");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithLimitedStackClass extends SqlTestBase {
        @ProfileConnection(level = Level.TRACE
                         , displayStackTrace = true
                         , filterStackTrace = false
                         , stackDepth = 5
                         , writerFactory = FileWriterBuilderForLimitedStack.class
        )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                statement.executeQuery();
            }
        }
    }

    public static class FileWriterBuilderForLimitedStack implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH_STACK_DEPTH);
        }
    }

    @Test public void
    should_profile_connection_with_limited_stack_depth() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionWithLimitedStackClass.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        String profilingResult = readContentOf(FILE_PATH_STACK_DEPTH);
        assertThat(profilingResult).hasLineCount(3 + (3 * 5));

    }

    public static class FileWriterBuilderForStackWithoutFiltering implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH_STACK_WITHOUT_FILTERING);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithStackWithoutFiltering extends SqlTestBase {
        @ProfileConnection(displayStackTrace = true
                         , filterStackTrace = false
                         , writerFactory = FileWriterBuilderForStackWithoutFiltering.class
        )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_not_filter_stack_trace() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionWithStackWithoutFiltering.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        String profilingResult = readContentOf(FILE_PATH_STACK_WITHOUT_FILTERING);
        assertThat(profilingResult).contains("org.junit.runners")
                                   .doesNotContain("null");

    }

    public static class FileWriterBuilderForProfilingBeforeAndAfterTestMethod implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(FILE_PATH_PROFILING_BEFORE_AND_AFTER_TEST_METHOD);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionBeforeAndAfterTestMethod extends SqlTestBase {
        @ProfileConnection( displayStackTrace = true
                          , filterStackTrace = false
                          , beforeAndAfterTestMethodExecution = true
                          , writerFactory = FileWriterBuilderForProfilingBeforeAndAfterTestMethod.class
        )
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_profile_connection_before_method_execution() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionBeforeAndAfterTestMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

        String profilingResult = readContentOf(FILE_PATH_PROFILING_BEFORE_AND_AFTER_TEST_METHOD);
        int closedConnectionNumber = profilingResult.split("- java.sql.Connection.close()").length - 1;
        // Because of connection pool initialization in SqlTestBase.before()
        assertThat(closedConnectionNumber).isGreaterThan(1);

    }

}
