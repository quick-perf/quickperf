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
package org.quickperf.spring.springboottest;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.quickperf.spring.springboottest.service.SpringDbConnectionProfiling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class SpringConnectionProfilingTest {

    @Test public void
    should_profile_connection() throws IOException {

        // GIVEN
        Class<?> testClass = SpringDbConnectionProfiling.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

        String profilingResult = readContentOf(SpringDbConnectionProfiling.DB_PROFILING_FILE_PATH);

        assertThat(profilingResult.replaceAll("connection .* -", "connection id -")
                                  .replaceAll("java:.*\\)", "java:lineNumber\\)")
                  )
                .startsWith("connection id - javax.sql.DataSource.getConnection()" + System.lineSeparator()
                          + "\tcom.zaxxer.hikari.HikariDataSource"
                           )
                .contains("\torg.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                        + "connection id - java.sql.Connection.setReadOnly(boolean readOnly) [readOnly: true]" + System.lineSeparator()
                        + "\torg.springframework.jdbc.datasource.DataSourceUtils.prepareConnectionForTransaction(DataSourceUtils.java:lineNumber)"
                         )
                .contains("org.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                        + "connection id - java.sql.Connection.setAutoCommit(boolean autoCommit) [autoCommit: false]" + System.lineSeparator()
                        + "\torg.hibernate.resource.jdbc.internal.AbstractLogicalConnectionImplementor.begin(AbstractLogicalConnectionImplementor.java:lineNumber)"
                         )
                .contains("\torg.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                        + "connection id - java.sql.Connection.commit() [isolation: transaction_read_committed]" + System.lineSeparator()
                        + "\torg.hibernate.resource.jdbc.internal.AbstractLogicalConnectionImplementor.commit(AbstractLogicalConnectionImplementor.java:lineNumber)")
                .contains("\torg.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                         + "connection id - java.sql.Connection.setAutoCommit(boolean autoCommit) [autoCommit: true]" + System.lineSeparator()
                         + "\torg.hibernate.resource.jdbc.internal.AbstractLogicalConnectionImplementor.resetConnection(AbstractLogicalConnectionImplementor.java:lineNumber)")
                .contains("\torg.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                        + "connection id - java.sql.Connection.setReadOnly(boolean readOnly) [readOnly: false]" + System.lineSeparator()
                        + "\torg.springframework.jdbc.datasource.DataSourceUtils.resetConnectionAfterTransaction(DataSourceUtils.java:lineNumber)")
                .contains("\torg.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)" + System.lineSeparator()
                        + "connection id - java.sql.Connection.close()" + System.lineSeparator()
                        + "\torg.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.closeConnection(DatasourceConnectionProviderImpl.java:lineNumber)")
                .endsWith("org.quickperf.spring.springboottest.service.SpringDbConnectionProfiling.should_find_all_players_with_team_name(SpringDbConnectionProfiling.java:lineNumber)")
        ;

    }

    private String readContentOf(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .collect(joining(System.lineSeparator()));
    }

}
