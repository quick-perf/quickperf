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
package org.quickperf.spring.springboottest.datasourcedelegation;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

final class ReadOnlyRoute {

    private ReadOnlyRoute() {
    }

    static DataSource anH2DataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .build();
    }

    // JdbcDataSource is a final class.
    static DataSource aFinalH2DataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        return dataSource;
    }

    // Only the read-only database has this table, so a select sent to the target fails.
    static DataSource withReadOnlyTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE READ_ONLY_TABLE (ID INT)");
        }
        catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return dataSource;
    }

    // LazyConnectionDataSourceProxy picks the DataSource when the first statement is created,
    // based on the read-only flag set before it.
    static void selectThrough(DataSource dataSource, boolean readOnly) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(readOnly);
            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(readOnly ? "SELECT * FROM READ_ONLY_TABLE" : "SELECT 1");
            }
        }
    }
}
