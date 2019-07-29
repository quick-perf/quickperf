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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.sql.batch;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.TestDataSourceBuilder;
import org.quickperf.sql.annotation.ExpectJdbcBatching;
import org.quickperf.sql.entities.Book;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Properties;

import static org.quickperf.sql.HibernateConfigBuilder.*;
import static org.quickperf.sql.PersistenceUnitInfoBuilder.*;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

@RunWith(QuickPerfJUnitRunner.class)
public class AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm {

    private static final int BATCH_SIZE = 30;

    private EntityManagerFactory emf;

    @Before
    public void before() {
        PersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
        PersistenceUnitInfo info = buildPersistenceUnitInfo();
        emf = persistenceProvider.createContainerEntityManagerFactory(info, new HashMap<>());
    }

    private PersistenceUnitInfo buildPersistenceUnitInfo() {
        DataSource baseDataSource = TestDataSourceBuilder.aDataSource().build();
        ProxyDataSource proxyDataSource = aDataSourceBuilder()
                                         .buildProxy(baseDataSource);
        Properties hibernateProperties = anHibernateConfig()
                                        .withBatchSize(BATCH_SIZE)
                                        .build();
        return aPersistenceUnitInfo().build(  proxyDataSource
                                            , hibernateProperties
                                            , Book.class);
    }

    @Test
    @ExpectJdbcBatching(batchSize = BATCH_SIZE)
    @HeapSize(value = 20,unit = AllocationUnit.MEGA_BYTE)
    public void execute_inserts_queries_in_batch_mode() {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        for (int i = 0; i < 1_000; i++) {
            Book newBook = new Book();
            newBook.setTitle("new book");
            if (i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            entityManager.persist(newBook);
        }
        transaction.commit();
    }

}
