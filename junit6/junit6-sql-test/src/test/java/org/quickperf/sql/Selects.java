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

import org.quickperf.sql.config.MemoryDataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

final class Selects {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    private Selects() { }

    static void executeSelects(int numberOfSelects) {
        for (int i = 0; i < numberOfSelects; i++) {
            executeASelect();
        }
    }

    static void executeASelect() {
        executeSql("select 1");
    }

    static void executeSql(String sql) {
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
