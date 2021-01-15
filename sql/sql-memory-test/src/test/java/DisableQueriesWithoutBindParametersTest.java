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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableQueriesWithoutBindParametersTest {

    private static final String QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE = "[PERF] Query without bind parameters";

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertStatementWithBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "INSERT INTO book VALUES (:id, :title, :isbn)";
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("id", 50L)
                    .setParameter("title", "CLEAN CODE")
                    .setParameter("isbn", "978-03213566974s");
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_succeed_when_insert_statement_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = InsertStatementWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class InsertStatementWithOneBindParameterAndUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "INSERT INTO book VALUES (:id,:title,'978-03213566974s')";
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("id", 50L)
                    .setParameter("title", "CLEAN CODE");
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_fail_when_insert_statement_with_one_bind_parameter_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = InsertStatementWithOneBindParameterAndUnbindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlUpdateStatementWithBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "UPDATE book SET isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_fail_when_update_statement_with_one_bind_parameter() {

        // GIVEN
        Class<?> testClass = SqlUpdateStatementWithBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlUpdateStatementWithUnBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "UPDATE book SET isbn = 978-0321356680";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_fail_when_update_statement_with_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlUpdateStatementWithUnBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlDeleteStatementWithOneBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "DELETE FROM book WHERE id = :id";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("id", 40);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_succeed_when_delete_statement_with_bind_parameter() {

        // GIVEN
        Class<?> testClass = SqlDeleteStatementWithOneBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlDeleteStatementWithoutBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "DELETE FROM book WHERE id = 40";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_fail_when_delete_statement_without_bind_parameter() {

        // GIVEN
        Class<?> testClass = SqlDeleteStatementWithoutBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlUpdateStatementWithBindParameters extends SqlTestBase {
        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "UPDATE book SET isbn = :isbn,title = :title WHERE id = :id";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680")
                    .setParameter("title", "Effective Java")
                    .setParameter("id", 40L);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_succeed_when_update_statement_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = SqlUpdateStatementWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlUpdateStatementWithOneBindParameterAndOneUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            String sql = "UPDATE book SET isbn = :isbn WHERE id = 40";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }
    }

    @Test
    public void should_fail_when_update_statement_with_one_bind_parameter_and_one_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlUpdateStatementWithOneBindParameterAndOneUnbindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }


}