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
import org.quickperf.sql.SqlTestBase;
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
import static org.quickperf.sql.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

@RunWith(QuickPerfJUnitRunner.class)
public class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert extends SqlTestBase {

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
    @ExpectJdbcBatching(batchSize = 30)
    public void execute_insert_queries_in_batch_mode_with_batchSize_a_multiple_of_rows_to_insert() {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        for (int i = 0; i < 60; i++) {
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
