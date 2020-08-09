/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelectedColumn;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;


public class MariaDbTest {

    @Testcontainers
    @QuickPerfTest
    public static class Select3ColumnsBut2Expected {

        @Container
        static final MariaDBContainer jdbcDatabaseContainer = new MariaDBContainer<>("mariadb:10.5.2")
                                                        .withDatabaseName("testcontainers")
                                                        .withUsername("nes")
                                                        .withPassword("quick");

        private final EntityManagerFactory entityManagerFactory = TcEntityManagerFactoryBuilder.build("org.hibernate.dialect.MariaDB103Dialect", jdbcDatabaseContainer);

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

}
