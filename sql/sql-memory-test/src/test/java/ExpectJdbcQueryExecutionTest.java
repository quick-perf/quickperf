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

import org.hibernate.internal.SessionImpl;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectJdbcQueryExecution;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectJdbcQueryExecutionTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneExecutionButZeroExpected extends SqlTestBase {

        @ExpectJdbcQueryExecution(0)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_jdbc_query_executions_is_not_equal_to_this_expected() {

        // GIVEN
        Class<?> testClass = OneExecutionButZeroExpected.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that there was <0> JDBC query execution")
                              .contains("But there is <1>");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneExecutionButZeroExpectedWithStatementAndBatching extends SqlTestBase {

        @ExpectJdbcQueryExecution(0)
        @Test
        public void execute_two_inserts_with_a_statement_and_batching() {

            Connection connection = getConnection();

            executeInATransaction(entityManager -> {
                try(Statement statement = connection.createStatement()) {
                    statement.addBatch("insert into Book (isbn, title, id) values ('isbn1', 'title1', 1)");
                    statement.addBatch("insert into Book (isbn, title, id) values ('isbn2', 'title2', 2)");
                    statement.executeBatch();
                } catch (SQLException sqlException) {
                    throw new IllegalStateException(sqlException);
                }
            });

        }

    }

    @Test public void
    should_fail_if_the_number_of_jdbc_query_executions_is_not_equal_to_this_expected_with_statement_and_batching() {

        // GIVEN
        Class<?> testClass = OneExecutionButZeroExpectedWithStatementAndBatching.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that there was <0> JDBC query execution")
                              .contains("But there is <1>");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoSameSelectTypeWithDifferentParameterValues extends SqlTestBase {

        @ExpectJdbcQueryExecution(1)
        @Test
        public void execute_two_same_select_type_with_two_diff_param_values() {

            EntityManager em = emf.createEntityManager();

            String paramName = "idParam";
            String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id=:" + paramName;

            Query query = em.createQuery(hqlQuery);
            query.setParameter(paramName, 2L);
            query.getResultList();

            Query query2 = em.createQuery(hqlQuery);
            query2.setParameter(paramName, 1L);
            query2.getResultList();

        }

    }

    @Test public void
    should_display_round_trip_and_n_plus_one_select_message_if_two_same_select_types_with_different_parameter_values() {

        // GIVEN
        Class<?> testClass = TwoSameSelectTypeWithDifferentParameterValues.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("ou may think that there was <1> JDBC query execution")
                              .contains("server roundtrips")
                              .contains("N+1");

    }

}
