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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectMaxInsert;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectMaxInsertTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectMaxInsert extends SqlTestBase {

        @Test
        @ExpectMaxInsert(0)
        public void execute_one_insert_but_no_insert_expected() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book effectiveJava = new Book();
            effectiveJava.setIsbn("effectiveJavaIsbn");
            effectiveJava.setTitle("Effective Java");

            em.persist(effectiveJava);

            em.getTransaction().commit();
        }

    }

    @Test public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_the_number_expected_with_annotation_on_method() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("You may think that at most <0> insert statement was sent to the database")
                .contains("But in fact <1>...");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectMaxInsert(0)
    public static class AClassAnnotatedWithExpectMaxInsert extends SqlTestBase {

        @Test
        public void execute_one_insert_but_no_insert_expected() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book effectiveJava = new Book();
            effectiveJava.setIsbn("effectiveJavaIsbn");
            effectiveJava.setTitle("Effective Java");

            em.persist(effectiveJava);

            em.getTransaction().commit();
        }
    }

    @Test public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_the_number_expected_with_annotation_on_class() {

        // GIVEN
        Class<?> testClass = AClassAnnotatedWithExpectMaxInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("You may think that at most <0> insert statement was sent to the database")
                .contains("But in fact <1>...");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectMaxInsertAndHavingNoPerfIssue extends SqlTestBase {

        @ExpectMaxInsert(1)
        @Test
        public void execute_one_insert() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book effectiveJava = new Book();
            effectiveJava.setIsbn("effectiveJavaIsbn");
            effectiveJava.setTitle("Effective Java");

            em.persist(effectiveJava);

            em.getTransaction().commit();
        }

    }

    @Test public void
    should_not_fail_if_the_performance_property_is_respected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxInsertAndHavingNoPerfIssue.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}