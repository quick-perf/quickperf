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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectJdbcBatching;

import javax.persistence.Query;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.HibernateConfigBuilder.anHibernateConfig;

public class ExpectJdbcBatchingWithoutBatchSizeTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAPassingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize extends SqlTestBase {

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
    public static class AClassHavingAFailingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize extends SqlTestBase {

        @Test
        @ExpectJdbcBatching()
        public void execute_not_batched_insert_queries() {

            executeInATransaction(entityManager -> {
                String firstInsertQueryAsString = "INSERT INTO Book (id, title) VALUES (1200, 'Book title')";
                Query query = entityManager.createNativeQuery(firstInsertQueryAsString);
                query.executeUpdate();

                String secondInsertQueryAsString = "INSERT INTO Book (id, title) VALUES (1300, 'Book title')";
                Query secondInsertQuery = entityManager.createNativeQuery(secondInsertQueryAsString);
                secondInsertQuery.executeUpdate();

            });

        }

    }

    @Test public void
    should_fail_with_two_insert_queries_not_batched() {

        Class<?> testClass = AClassHavingAFailingMethodAnnotatedExpectJdbcBatchingWithoutBatchSize.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isEqualTo(1);
        assertThat(printableResult.toString())
                .contains("[PERF] SQL executions were supposed to be batched.");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingOneInsertNotBatched extends SqlTestBase {

        @Test
        @ExpectJdbcBatching()
        public void execute_one_insert_not_batched() {

            executeInATransaction(entityManager -> {
                String firstInsertQueryAsString = "INSERT INTO Book (id, title) VALUES (1200, 'Book title')";
                Query query = entityManager.createNativeQuery(firstInsertQueryAsString);
                query.executeUpdate();
            });

        }

    }

    @Test public void
    should_fail_with_one_insert_and_no_batch_mode() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingOneInsertNotBatched.class;

        PrintableResult testResult = PrintableResult.testResult(testClass);

        assertThat(testResult.failureCount()).isEqualTo(1);
        assertThat(testResult.toString()).contains("SQL executions were supposed to be batched");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingOneInsertBatched extends SqlTestBase {

        @Override
        protected Properties getHibernateProperties() {
            return   anHibernateConfig()
                    .withBatchSize(30)
                    .build();
        }

        @Test
        @ExpectJdbcBatching()
        public void execute_one_insert_in_batch_mode() {

            executeInATransaction(entityManager -> {
                Book newBook = new Book();
                newBook.setTitle("new book");
                entityManager.persist(newBook);
            });

        }

    }

    @Test public void
    should_pass_with_one_insert_and_batch_mode() {

        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectJdbcBatchingAndExecutingOneInsertBatched.class;

        PrintableResult testResult = PrintableResult.testResult(testClass);

        assertThat(testResult.failureCount()).isEqualTo(0);

    }

}
