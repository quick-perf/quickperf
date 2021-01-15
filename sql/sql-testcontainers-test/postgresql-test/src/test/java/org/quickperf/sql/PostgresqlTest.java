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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelectedColumn;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class PostgresqlTest {

    private static final PostgreSQLContainer jdbcDatabaseContainer
            = new PostgreSQLContainer<>("postgres:12.3")
             .withDatabaseName("testcontainers")
             .withUsername("nes")
             .withPassword("quick");

    @BeforeAll
    public static void startContainer() {
        jdbcDatabaseContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        jdbcDatabaseContainer.stop();
    }

    @QuickPerfTest
    public static class Select3ColumnsBut2Expected {

        private final EntityManagerFactory entityManagerFactory = TcEntityManagerFactoryBuilder.build("org.hibernate.dialect.PostgreSQLDialect", jdbcDatabaseContainer);

        @ExpectSelectedColumn(2)
        @Test
        public void select_with_three_columns() {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Query query = entityManager.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_selected_columns_is_not_this_expected() {

        // GIVEN
        Class<?> testClass = Select3ColumnsBut2Expected.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult testsResult = jUnit5Tests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailures()).isOne();

        assertThat(testsResult.getErrorReport())
                .contains("You may think that <2> columns were selected")
                .contains("But in fact <3>...");

    }

    @QuickPerfTest
    public static class NoResultSet {

        private final EntityManagerFactory entityManagerFactory = TcEntityManagerFactoryBuilder.build("org.hibernate.dialect.PostgreSQLDialect", jdbcDatabaseContainer);

        @ExpectSelectedColumn(2)
        @Test
        public void select_with_three_columns() {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            Query query = entityManager.createNativeQuery("SELECT * FROM BOOK" +" syntax error"
            );
            query.getResultList();
        }

    }

    @Test public void
    error_report_should_contain_database_exception_and_executed_SQL_if_improper_SQL() {

        // GIVEN
        Class<?> testClass = NoResultSet.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult testsResult = jUnit5Tests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailures()).isOne();

        String errorReport = testsResult.getErrorReport();
        assertThat(errorReport)
                .contains("org.postgresql.util.PSQLException")
                .contains("SELECT")
                .contains("FROM");

    }

}
