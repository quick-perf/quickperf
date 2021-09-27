/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Before;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.sql.config.MemoryDatabaseHibernateDialect;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;
import java.util.function.Consumer;

import static org.quickperf.sql.config.HibernateConfigBuilder.anHibernateConfig;
import static org.quickperf.sql.config.PersistenceUnitInfoBuilder.*;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

public class SqlTestBaseJUnit4 {

    protected EntityManagerFactory emf;

    @Before
    public void before() {
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        PersistenceUnitInfo info = buildPersistenceUnitInfo();
        emf = persistenceProvider.createContainerEntityManagerFactory(info, new HashMap<>());
    }

    private PersistenceUnitInfo buildPersistenceUnitInfo() {
        DataSource baseDataSource = MemoryDataSourceBuilder.aDataSource().build();
        ProxyDataSource proxyDataSource = aDataSourceBuilder().buildProxy(baseDataSource);
        Properties hibernateProperties = getHibernateProperties();
        return aPersistenceUnitInfo().build(proxyDataSource
                                          , hibernateProperties
                                          , Book.class);
    }

    Properties getHibernateProperties() {
        String hibernateDialect = MemoryDatabaseHibernateDialect.INSTANCE.getHibernateDialect();
        return anHibernateConfig().build(hibernateDialect);
    }

    Connection getConnection() {
        EntityManager em = emf.createEntityManager();
        SessionImpl session = (SessionImpl) em.getDelegate();
        return session.connection();
    }

}
