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
package org.quickperf.testng.sql;

import org.quickperf.testng.TestNGTests;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class ProfileConnectionTestNGTest {

    @Test public void
    should_profile_database_connection() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionTestNG.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isZero();

        String profilingResult = readContentOf(ProfileConnectionTestNG.FILE_PATH);

        assertThat(profilingResult.replaceAll("connection .* -", "connection id -")
                                  .replaceAll("java:.*\\)", "java:lineNumber\\)")
                  )
                .isEqualToNormalizingNewlines(
                         "connection id - javax.sql.DataSource.getConnection()\n" +
                        "\torg.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection(DatasourceConnectionProviderImpl.java:lineNumber)" + System.lineSeparator() +
                        "\torg.hibernate.internal.NonContextualJdbcConnectionAccess.obtainConnection(NonContextualJdbcConnectionAccess.java:lineNumber)" + System.lineSeparator() +
                        "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.acquireConnectionIfNeeded(LogicalConnectionManagedImpl.java:lineNumber)" + System.lineSeparator() +
                        "\torg.hibernate.resource.jdbc.internal.LogicalConnectionManagedImpl.getPhysicalConnection(LogicalConnectionManagedImpl.java:lineNumber)" + System.lineSeparator() +
                        "\torg.hibernate.internal.SessionImpl.connection(SessionImpl.java:lineNumber)" + System.lineSeparator() +
                        "\torg.quickperf.testng.sql.SqlTestBaseTestNG.getConnection(SqlTestBaseTestNG.java:lineNumber)" + System.lineSeparator() +
                        "\torg.quickperf.testng.sql.ProfileConnectionTestNG$ProfileConnectionClass.test(ProfileConnectionTestNG.java:lineNumber)" + System.lineSeparator() +
                        "connection id - java.sql.Connection.prepareStatement(String sql) [sql: select isbn from Book]" + System.lineSeparator() +
                        "\torg.quickperf.testng.sql.ProfileConnectionTestNG$ProfileConnectionClass.test(ProfileConnectionTestNG.java:lineNumber)" + System.lineSeparator() +
                        "connection id - java.sql.Connection.close()" + System.lineSeparator() +
                        "\torg.quickperf.testng.sql.ProfileConnectionTestNG$ProfileConnectionClass.test(ProfileConnectionTestNG.java:lineNumber)"
                )
        ;

    }

    private String readContentOf(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .collect(joining(System.lineSeparator()));
    }


}
