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
package org.quickperf.spring.datasourceexception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Verifies that a test getting a connection from a DataSource bean that refuses it fails
 * with the SQLException thrown by the DataSource.
 */
@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(classes = SqlExceptionOfDataSource.Config.class)
public class SqlExceptionOfDataSource {

    @Autowired
    private DataSource dataSource;

    @Test
    public void get_and_close_a_connection() throws SQLException {
        dataSource.getConnection().close();
    }

    // Spring 3 needs a no-argument constructor to proxy this class with CGLIB.
    public static class DataSourceRefusingConnections extends AbstractDataSource {

        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("Connection refused");
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("Connection refused");
        }
    }

    @Configuration
    static class Config {

        @Bean
        public DataSource dataSource() {
            return new DataSourceRefusingConnections();
        }

        @Bean
        public static QuickPerfProxyBeanPostProcessor dataSourceBeanPostProcessor() {
            return new QuickPerfProxyBeanPostProcessor();
        }
    }
}
