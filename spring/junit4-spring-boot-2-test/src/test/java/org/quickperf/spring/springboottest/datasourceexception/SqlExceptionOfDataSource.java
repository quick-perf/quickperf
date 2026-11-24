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
package org.quickperf.spring.springboottest.datasourceexception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.springboottest.FootballApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Verifies that a test getting a connection from a DataSource bean that refuses it fails
 * with the SQLException thrown by the DataSource.
 */
@RunWith(QuickPerfSpringRunner.class)
@SpringBootTest(classes = {FootballApplication.class})
@Import(SqlExceptionOfDataSource.Config.class)
public class SqlExceptionOfDataSource {

    @Autowired
    @Qualifier("dataSourceRefusingConnections")
    private DataSource dataSourceRefusingConnections;

    @Test
    public void get_and_close_a_connection() throws SQLException {
        dataSourceRefusingConnections.getConnection().close();
    }

    static class DataSourceRefusingConnections extends AbstractDataSource {

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("Connection refused");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("Connection refused");
        }
    }

    static class Config {

        // Declaring a DataSource bean disables the one auto-configured by Spring Boot. This working
        // database replaces it: JPA gets a connection from the primary DataSource at startup.
        @Bean
        @Primary
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .generateUniqueName(true)
                    .build();
        }

        // The DataSource of the test method, injected with @Qualifier because the other one is primary.
        @Bean
        DataSource dataSourceRefusingConnections() {
            return new DataSourceRefusingConnections();
        }
    }
}
