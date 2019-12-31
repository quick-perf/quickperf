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

package org.quickperf.sql;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.annotation.ExpectJdbcBatching;

import javax.persistence.Query;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.HibernateConfigBuilder.anHibernateConfig;

public class ExpectJdbcBatchingJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert extends SqlTestBaseJUnit4 {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = 30)
        public void execute_insert_queries_in_batch_mode_with_batchSize_a_multiple_of_rows_to_insert() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 60; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }

    }

    @Test public void
    a_method_annotated_with_jdbc_batches_and_with_batch_size_a_multiple_of_rows_to_insert() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeAMultipleOfRowsToInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert extends SqlTestBaseJUnit4 {

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

            executeInATransaction(entityManager -> {
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
            });

        }

    }

    @Test public void
    should_not_fail_if_last_batch_execution_has_a_batch_size_different_from_the_expected_batch_size() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithJdbcBatchAndWithBatchSizeNOTAMultipleOfRowsToInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsertInNewJvm extends SqlTestBaseJUnit4 {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = BATCH_SIZE)
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        public void execute_inserts_queries_in_batch_and_native_query_not_batched_in_new_Jvm() {

            executeInATransaction(entityManager -> {
                String nativeQuery = "INSERT INTO Book (title,id) VALUES ('Book title',1200)";
                Query query = entityManager.createNativeQuery(nativeQuery);
                query.executeUpdate();
            });

        }

    }

    @Test public void
    a_method_annotated_with_jdbc_batches_and_insert_not_batched_in_new_jvm() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsertInNewJvm.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm extends SqlTestBaseJUnit4 {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = BATCH_SIZE)
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        public void execute_inserts_queries_in_batch_mode() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 1_000; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }
    }

    @Test public void
    a_method_annotated_with_jdbc_batches_in_new_jvm() {

        Class<?> testClass = AClassHavingAMethodAnnotatedExpectJdbcBatchingInNewJvm.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsert extends SqlTestBaseJUnit4 {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching(batchSize = BATCH_SIZE)
        public void execute_a_not_batched_query() {

            executeInATransaction(entityManager -> {
                String nativeQuery = "INSERT INTO Book (title,id) VALUES ('Book title',1200)";
                Query query = entityManager.createNativeQuery(nativeQuery);
                query.executeUpdate();
            });

        }

    }

    @Test public void
    should_fail_with_a_method_annotated_with_jdbc_batches_and_query_not_batched() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingANotBatchedInsert.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);
        assertThat(printableResult.toString())
                .contains("[PERF] Expected batch size <30> but is <0>.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAPassingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize extends SqlTestBaseJUnit4 {

        private static final int BATCH_SIZE = 30;

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(BATCH_SIZE)
                    .build();
        }

        @Test
        @ExpectJdbcBatching()
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        public void execute_insert_queries_in_batch_mode() {

            executeInATransaction(entityManager -> {
                for (int i = 0; i < 1_000; i++) {
                    Book newBook = new Book();
                    newBook.setTitle("new book");
                    if (i % BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    entityManager.persist(newBook);
                }
            });

        }

    }

    @Test public void
    should_verify_that_sql_orders_are_batched_without_checking_batch_size() {

        Class<?> testClass = AClassHavingAPassingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAFailingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize extends SqlTestBaseJUnit4 {

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(30)
                    .build();
        }

        @Test
        @ExpectJdbcBatching()
        public void execute_a_not_batched_query() {

            executeInATransaction(entityManager -> {
                String nativeQuery = "INSERT INTO Book (title,id) VALUES ('Book title',1200)";
                Query query = entityManager.createNativeQuery(nativeQuery);
                query.executeUpdate();
            });

        }

    }

    @Test public void
    should_fail_with_not_batched_query_and_no_batch_size_parameter_in_expect_jdbc_batching_annotation() {

        Class<?> testClass = AClassHavingAFailingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);
        assertThat(printableResult.toString())
                .contains("[PERF] SQL executions were supposed to be batched.");

    }

}
