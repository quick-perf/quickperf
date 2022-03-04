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

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.quickperf.sql.config.HibernateConfigBuilder;
import org.quickperf.sql.config.PersistenceUnitInfoBuilder;
import org.testcontainers.containers.JdbcDatabaseContainer;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;

import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

public class TcEntityManagerFactoryBuilder {

    private TcEntityManagerFactoryBuilder() { }

    public static EntityManagerFactory build(String dialect, JdbcDatabaseContainer jdbcDatabaseContainer) {
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        PersistenceUnitInfo info = buildPersistenceUnitInfo(dialect, jdbcDatabaseContainer);
        return persistenceProvider.createContainerEntityManagerFactory(info, new HashMap<>());
    }

    private static PersistenceUnitInfo buildPersistenceUnitInfo(String dialect, JdbcDatabaseContainer jdbcDatabaseContainer) {
        DataSource baseDataSource = TestContainerDatasourceBuilder.aDataSource().buildFrom(jdbcDatabaseContainer);
        ProxyDataSource proxyDataSource = aDataSourceBuilder()
                                         .buildProxy(baseDataSource);
        Properties hibernateProperties = HibernateConfigBuilder.anHibernateConfig().build(dialect);
        return PersistenceUnitInfoBuilder.aPersistenceUnitInfo()
                .build(   proxyDataSource
                        , hibernateProperties
                        , Book.class);
    }

    private static class TestContainerDatasourceBuilder {

        private TestContainerDatasourceBuilder() { }

        public static TestContainerDatasourceBuilder aDataSource() {
            return new TestContainerDatasourceBuilder();
        }

        public DataSource buildFrom(JdbcDatabaseContainer jdbc) {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName(jdbc.getDriverClassName());
            dataSource.setUrl(jdbc.getJdbcUrl());
            dataSource.setUsername(jdbc.getUsername());
            dataSource.setPassword(jdbc.getPassword());
            dataSource.setMaxActive(4);
            dataSource.setPoolPreparedStatements(true);
            return dataSource;
        }

    }

}
