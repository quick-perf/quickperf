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
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectMaxJdbcQueryExecution;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExpectMaxJdbcQueryExecutionTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoJdbcQueryExecutionsButOneExpected extends SqlTestBase {

        @ExpectMaxJdbcQueryExecution(1)
        @Test
        public void execute_two_selects() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_number_of_jdbc_query_executions_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = TwoJdbcQueryExecutionsButOneExpected.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that there was at most <1> JDBC query execution")
                              .contains("But there are <2>...");


    }

}
