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
import org.quickperf.sql.annotation.ExpectQueriesSending;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectQueriesSendingTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneExecutionButZeroExpected extends SqlTestBase {

        @ExpectQueriesSending(0)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_queries_sendings_is_not_equal_to_this_expected() {

        // GIVEN
        Class<?> testClass = OneExecutionButZeroExpected.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that there was <0> queries sending")
                              .contains("But there is <1>");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneExecutionButZeroExpectedWithStatementAndBatching extends SqlTestBase {

        @ExpectQueriesSending(0)
        @Test
        public void execute_two_inserts_with_a_statement_and_batching() {

            EntityManager em = emf.createEntityManager();
            SessionImpl session = (SessionImpl) em.getDelegate();
            Connection connection = session.connection();

            executeInATransaction(entityManager -> {
                try {
                    Statement statement = connection.createStatement();
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
    should_fail_if_the_number_of_queries_sendings_is_not_equal_to_this_expected_with_statement_and_batching() {

        // GIVEN
        Class<?> testClass = OneExecutionButZeroExpectedWithStatementAndBatching.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that there was <0> queries sending")
                              .contains("But there is <1>");

    }

}
