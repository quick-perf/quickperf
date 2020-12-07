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
import org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableQueriesWithoutBindParametersTest {

    private static final String QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE = "[PERF] Query without bind parameters";

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = :isbn AND title = :title";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680")
                    .setParameter("title", "Effective Java");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_succeed_when_statement_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = StatementWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithOneBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_succeed_when_statement_with_one_bind_parameter() {

        // GIVEN
        Class<?> testClass = StatementWithOneBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithoutWhereClause extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }


    }

    @Test
    public void should_succeed_when_statement_without_where_clause() {

        // GIVEN
        Class<?> testClass = StatementWithoutWhereClause.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithOneBindParameterAndOneUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = :isbn AND title = 'Effective Java'";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }


    }

    @Test
    public void should_have_one_failure_when_statement_with_one_bind_parameter_and_one_unbind_parameter() {

        // GIVEN
        Class<?> testClass = StatementWithOneBindParameterAndOneUnbindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithoutBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = '978-0321356680'";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }


    }

    @Test
    public void should_have_one_failure_when_statement_without_bind_parameters() {

        // GIVEN
        Class<?> testClass = StatementWithoutBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }


    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithBindParametersAndOr extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = :isbn OR title = :title";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680")
                    .setParameter("title", "Effective Java");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_succeed_when_statement_with_bind_parameters_and_or() {

        // GIVEN
        Class<?> testClass = StatementWithBindParametersAndOr.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithOneBindParameterAndOneUnbindParameterAndOr extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn = :isbn OR title = 'Effective Java'";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_with_one_bind_parameters_and_one_unbind_parameter_and_or() {

        // GIVEN
        Class<?> testClass = StatementWithOneBindParameterAndOneUnbindParameterAndOr.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementWithOneUnbindParameterAtFirstPosition extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE title = 'Effective Java' OR isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_with_one_un_bind_parameter_at_first_position() {

        // GIVEN
        Class<?> testClass = StatementWithOneUnbindParameterAtFirstPosition.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingLowerCaseSQLKeywordsAndOneUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "select * from book where title = 'Effective Java' or isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_using_lower_case_sql_keywords_and_one_un_bind_parameter() {

        // GIVEN
        Class<?> testClass = StatementUsingLowerCaseSQLKeywordsAndOneUnbindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementHavingAKeyWordWithinValues extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "select * from book where title = 'or isbn = ?' or isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_with_one_tricky_unbind_parameter() {

        // GIVEN
        Class<?> testClass = StatementHavingAKeyWordWithinValues.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }


    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementHavingAKeyWordAndAQuoteWithinValues extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "select * from book where title = 'or isbn ''=?' or isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_with_one_tricky_containing_quote_unbind_parameter() {

        // GIVEN
        Class<?> testClass = StatementHavingAKeyWordAndAQuoteWithinValues.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingBetweenWithBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id BETWEEN :min AND :max";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("min", 40)
                    .setParameter("max", 41);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_statement_using_between_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = StatementUsingBetweenWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingBetweenWithOneBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id BETWEEN :value AND 41";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value", 40);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_statement_using_between_with_one_bind_parameter() {

        // GIVEN
        Class<?> testClass = StatementUsingBetweenWithOneBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingInWithBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (:value1,:value2)";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value1", 40)
                    .setParameter("value2", 41);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_statement_using_in_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = StatementUsingInWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingInWithOneBindParameterLeft extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (:value,41)";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value", 40);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_statement_using_in_with_one_bind_parameter_left() {

        // GIVEN
        Class<?> testClass = StatementUsingInWithOneBindParameterLeft.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingInWithOneBindParameterRight extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (41,:value)";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value", 40);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_statement_using_in_with_one_bind_parameter_right() {

        // GIVEN
        Class<?> testClass = StatementUsingInWithOneBindParameterRight.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingInWithOneBindParameterBetweenUnbindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (41,:value,42)";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value", 40);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_statement_using_in_with_one_bind_parameter_between_unbind_parameters() {

        // GIVEN
        Class<?> testClass = StatementUsingInWithOneBindParameterBetweenUnbindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingLikeWithBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn LIKE :value";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("value", "978-%");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_statement_using_like_with_bind_parameter() {

        // GIVEN
        Class<?> testClass = StatementUsingLikeWithBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class StatementUsingLikeWithUnBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE isbn LIKE '978-%'";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_statement_using_like_with_unbind_parameter() {

        // GIVEN
        Class<?> testClass = StatementUsingLikeWithUnBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NestedStatementWithBindParameters extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT b.title FROM (SELECT * FROM book WHERE id=:id) b WHERE isbn LIKE :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("id", 40L)
                    .setParameter("isbn", "978-%");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_nested_statement_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = NestedStatementWithBindParameters.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NestedStatementWithOneBindParameterAndUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (SELECT b.id FROM book b WHERE isbn =:isbn OR title = 'Effective Java')";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-%");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_nested_statement_with_one_bind_parameter_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = NestedStatementWithOneBindParameterAndUnbindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NestedStatementWithOneBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE id IN (SELECT b.id FROM book b WHERE isbn =:isbn)";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-%");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_nested_statement_with_one_bind_parameter() {

        // GIVEN
        Class<?> testClass = NestedStatementWithOneBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

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
    public static class StatementUsingUpperCaseSQLKeywordsAndOneUnbindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT * FROM book WHERE title = 'EFFECTIVE JAVA' or isbn = :isbn";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("isbn", "978-0321356680");
            nativeQuery.getResultList();
        }

    }

    @Test
    public void should_fail_when_statement_using_upper_case_sql_keywords_and_one_un_bind_parameter() {

        // GIVEN
        Class<?> testClass = StatementUsingUpperCaseSQLKeywordsAndOneUnbindParameter.class;

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

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlJoinStatementWithBindParameter extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a, Book b WHERE a.title = b.title";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_sql_join_statement_with_bind_parameter() {

        // GIVEN
        Class<?> testClass = SqlJoinStatementWithBindParameter.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlWhereWithQuotesAfterEquals extends SqlTestBase {

        @DisableQueriesWithoutBindParameters
        @Test
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE a.title = 'Effective Java'";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_sql_join_statement_with_quotes_after_equals_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlWhereWithQuotesAfterEquals.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlWhereWithQuotesBeforeEquals extends SqlTestBase {

        @DisableQueriesWithoutBindParameters
        @Test
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE 'Effective Java' = a.title";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_sql_join_statement_with_quotes_before_equals_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlWhereWithQuotesBeforeEquals.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlWhereWithNumberAfterEquals extends SqlTestBase {

        @DisableQueriesWithoutBindParameters
        @Test
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE a.id = 40";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_sql_join_statement_with_number_after_equals_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlWhereWithNumberAfterEquals.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlWhereWithNumberBeforeEquals extends SqlTestBase {

        @DisableQueriesWithoutBindParameters
        @Test
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE 40 = a.title";
            Query nativeQuery = em.createNativeQuery(sql);
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_fail_when_sql_join_statement_with_number_before_equals_and_unbind_parameter() {

        // GIVEN
        Class<?> testClass = SqlWhereWithNumberBeforeEquals.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlJoinStatementWithBindParameterAfterEqual extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE a.title = :title";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("title", "Effective Java");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_sql_join_statement_with_bind_parameter_after_equal() {

        // GIVEN
        Class<?> testClass = SqlJoinStatementWithBindParameterAfterEqual.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlJoinStatementWithBindParameterBeforeEqual extends SqlTestBase {

        @Test
        @DisableQueriesWithoutBindParameters
        public void test_method() {
            EntityManager em = emf.createEntityManager();
            String sql = "SELECT a.title FROM Book a WHERE :title = a.title";
            Query nativeQuery = em.createNativeQuery(sql)
                    .setParameter("title", "Effective Java");
            nativeQuery.getResultList();
        }
    }

    @Test
    public void should_succeed_when_sql_join_statement_with_bind_parameter_before_equal() {

        // GIVEN
        Class<?> testClass = SqlJoinStatementWithBindParameterBeforeEqual.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();
    }

}