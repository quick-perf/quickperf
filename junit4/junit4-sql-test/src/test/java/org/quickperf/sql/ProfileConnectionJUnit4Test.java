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

public class ProfileConnectionJUnit4Test {

    private static final String FILE_PATH = findTargetPath() + File.separator + "junit4-connection-profiling.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionClass extends SqlTestBaseJUnit4 {
        @ProfileConnection( level = Level.TRACE
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
    should_profile_database_connection() throws IOException {

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
                .contains(
                                "connection id - javax.sql.DataSource.getConnection()" + System.lineSeparator() +
                                "\torg.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:lineNumber)" + System.lineSeparator() +
                                "\torg.hibernate.internal.NonContextualJdbcConnectionAccess.obtainConnection(NonContextualJdbcConnectionAccess.java:lineNumber)" + System.lineSeparator() +
                                "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded(LogicalConnectionManagedImpl.java:lineNumber)" + System.lineSeparator() +
                                "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.getPhysicalConnection(LogicalConnectionManagedImpl.java:lineNumber)" + System.lineSeparator() +
                                "\torg.hibernate.internal.SessionImpl.connection(SessionImpl.java:lineNumber)" + System.lineSeparator() +
                                "\torg.quickperf.sql.SqlTestBaseJUnit4.getConnection(SqlTestBaseJUnit4.java:lineNumber)" + System.lineSeparator() +
                                "\torg.quickperf.sql.ProfileConnectionJUnit4Test$ProfileConnectionClass.test(ProfileConnectionJUnit4Test.java:lineNumber)" + System.lineSeparator() +
                                "connection id - java.sql.Connection.prepareStatement(String sql) [sql: select isbn from Book]" + System.lineSeparator() +
                                "\torg.quickperf.sql.ProfileConnectionJUnit4Test$ProfileConnectionClass.test(ProfileConnectionJUnit4Test.java:lineNumber)" + System.lineSeparator() +
                                "connection id - java.sql.Connection.close()" + System.lineSeparator())
                .contains(      "\torg.quickperf.sql.ProfileConnectionJUnit4Test$ProfileConnectionClass.test(ProfileConnectionJUnit4Test.java:lineNumber)");

    }

    private String readContentOf(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .collect(joining(System.lineSeparator()));
    }

}
