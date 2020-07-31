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

public class DisableQueriesWithoutBindParametersTest {

    private static final String UNBOUND_PARAMETERS_MESSAGE = "[PERF] Unbound parameters detected";

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

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
    public void should_succeed_when_statement_with_one_bind_parameters_and_one_unbind_parameter_and_or() {

        // GIVEN
        Class<?> testClass = StatementWithOneBindParameterAndOneUnbindParameterAndOr.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
    public void should_succeed_when_statement_with_one_un_bind_parameter_at_first_position() {

        // GIVEN
        Class<?> testClass = StatementWithOneUnbindParameterAtFirstPosition.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

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
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();
        assertThat(printableResult.toString()).contains(UNBOUND_PARAMETERS_MESSAGE);

    }

}