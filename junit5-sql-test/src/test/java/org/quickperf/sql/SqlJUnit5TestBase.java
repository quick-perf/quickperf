package org.quickperf.sql;/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2019 the original author or authors.
 */

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.quickperf.sql.conf.HibernateConfigBuilder;
import org.quickperf.sql.conf.PersistenceUnitInfoBuilder;
import org.quickperf.sql.conf.TestDataSourceBuilder;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;

import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

public class SqlJUnit5TestBase {

    protected EntityManagerFactory emf;

    @BeforeEach
    public void before() {
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        PersistenceUnitInfo info = buildPersistenceUnitInfo();
        emf = persistenceProvider.createContainerEntityManagerFactory(info, new HashMap<>());
    }

    private PersistenceUnitInfo buildPersistenceUnitInfo() {
        DataSource baseDataSource = TestDataSourceBuilder.aDataSource().build();
        ProxyDataSource proxyDataSource = aDataSourceBuilder()
                                          .buildProxy(baseDataSource);
        Properties hibernateProperties = HibernateConfigBuilder.anHibernateConfig().build();
        return PersistenceUnitInfoBuilder.aPersistenceUnitInfo()
                                         .build(   proxyDataSource
                                                 , hibernateProperties
                                                 , Book.class);
    }

}
