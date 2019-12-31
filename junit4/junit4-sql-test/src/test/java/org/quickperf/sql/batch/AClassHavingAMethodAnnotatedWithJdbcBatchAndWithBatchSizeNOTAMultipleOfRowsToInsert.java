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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.SqlTestBaseJUnit4;
import org.quickperf.sql.annotation.ExpectJdbcBatching;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Properties;

import static org.quickperf.sql.config.HibernateConfigBuilder.anHibernateConfig;

@RunWith(QuickPerfJUnitRunner.class)
public class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert extends SqlTestBaseJUnit4 {

    private static final int BATCH_SIZE = 30;

    @Override
    protected Properties getHibernateProperties() {
        return   anHibernateConfig()
                .withBatchSize(BATCH_SIZE)
                .build();
    }

    @Test
    @ExpectJdbcBatching(batchSize = BATCH_SIZE)
    public void execute_inserts_queries_in_batch_mode_with_batch_size_NOT_a_multiple_of_rows_to_insert() {
        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        int numberOfElementsToInsertNotAMultipleOfBatchSize = 70;
        for (int i = 0; i < numberOfElementsToInsertNotAMultipleOfBatchSize; i++) {
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
