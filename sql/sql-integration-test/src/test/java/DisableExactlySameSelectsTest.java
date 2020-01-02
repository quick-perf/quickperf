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
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.DisableExactlySameSelects;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class DisableExactlySameSelectsTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableSameSqlForDifferentParamValues extends SqlTestBase {

        @Test
        @DisableExactlySameSelects
        public void execute_two_select_with_different_param_values() {
            EntityManager em = emf.createEntityManager();

            String hqlQuery = "FROM " + Book.class.getCanonicalName()
                            + " b WHERE b.id=:idParam AND b.title=:titleParam";

            Query query1 = em.createQuery(hqlQuery);
            query1.setParameter("idParam", 3L);
            query1.setParameter("titleParam", "titleParam1");
            query1.getResultList();

            Query query2 = em.createQuery(hqlQuery);
            query2.setParameter("idParam", 1L);
            query2.setParameter("titleParam", "titleParam1");
            query2.getResultList();

        }

        @Test
        @DisableExactlySameSelects
        public void execute_two_selects_with_different_param_names() {

            EntityManager em = emf.createEntityManager();
            String select = "FROM " + Book.class.getCanonicalName() + " b ";

            Query query1 = em.createQuery(select + "WHERE b.title=:title");
            query1.setParameter("title", "aTitle");
            query1.getResultList();

            Query query2 = em.createQuery(select + "WHERE b.isbn=:isbn");
            query2.setParameter("isbn", "anIsbn");
            query2.getResultList();

        }

    }

    @Test public void
    should_not_fail_if_exactly_same_select_with_diff_param_values() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableSameSqlForDifferentParamValues.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithSameParams extends SqlTestBase {

        @Test
        @DisableExactlySameSelects
        public void execute_two_same_select_with_same_params() {

            EntityManager em = emf.createEntityManager();

            String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id = :idParam";

            Query query1 = em.createQuery(hqlQuery);
            query1.setParameter("idParam", 3L);
            query1.getResultList();

            Query query2 = em.createQuery(hqlQuery);
            query2.setParameter("idParam", 3L);
            query2.getResultList();

        }

    }

    @Test public void
    should_fail_if_same_selects_with_same_params_and_test_method_annotated_disable_same_sql_select() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithSameParams.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isEqualTo(1);

        assertThat(printableResult.toString())
                      .contains("[PERF] Exactly same SELECT statements");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithoutParams extends SqlTestBase {

        @Test
        @DisableExactlySameSelects
        public void execute_two_same_selects_without_params() {

            EntityManager em = emf.createEntityManager();

            Query query1 = em.createQuery("FROM " + Book.class.getCanonicalName());
            query1.getResultList();

            Query query2 = em.createQuery("FROM " + Book.class.getCanonicalName());
            query2.getResultList();

        }

    }

    @Test public void
    should_fail_if_two_same_selects_without_params_and_test_method_annotated_disable_same_sql_select() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithoutParams.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isEqualTo(1);

        assertThat(printableResult.toString()).contains("Exactly same SELECT statements");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithInsertQueries extends SqlTestBase {

        @Test
        @DisableExactlySameSelects
        public void execute_two_insert_statements_who_should_pass() {

            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book newBook = new Book();
            newBook.setTitle("newTitle");
            em.persist(newBook);

            Book book2 = new Book();
            book2.setTitle("newTitle2");
            em.persist(book2);
            em.getTransaction().commit();

        }

    }

    @Test public void
    should_pass_if_two_inserts_and_test_annotated_with_disable_same_select_sql() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithInsertQueries.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isEqualTo(0);

    }

}
