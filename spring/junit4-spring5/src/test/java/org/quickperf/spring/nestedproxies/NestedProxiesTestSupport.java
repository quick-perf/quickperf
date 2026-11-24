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
package org.quickperf.spring.nestedproxies;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NestedProxiesTestSupport {

    private NestedProxiesTestSupport() { }

    static DataSource anH2DataSource(String databaseName) {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:" + databaseName);
        h2DataSource.setUser("qp");
        h2DataSource.setPassword("");
        return h2DataSource;
    }

    static void executeOneSelect(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select 1")) {
            statement.executeQuery();
        }
    }

    static String profilingFilePath(String fileName) {
        return Paths.get("target").toFile().getAbsolutePath() + File.separator + fileName;
    }

    public static void assertThatTheEventsOfOneSelectAreProfiledOnce(String profilingFilePath) throws IOException {
        List<String> profilingLines = Files.readAllLines(Paths.get(profilingFilePath), StandardCharsets.UTF_8);
        assertThat(profilingLines).hasSize(3);
        String connectionId = profilingLines.get(0).substring(0, profilingLines.get(0).indexOf(" - "));
        assertThat(profilingLines).containsExactly(
                connectionId + " - javax.sql.DataSource.getConnection()"
              , connectionId + " - java.sql.Connection.prepareStatement(String sql) [sql: select 1]"
              , connectionId + " - java.sql.Connection.close()");
    }

}
