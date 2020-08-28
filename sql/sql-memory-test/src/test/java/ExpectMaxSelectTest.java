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
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.Xmx;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectMaxSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExpectMaxSelectTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneSelectButNoSelectExpected extends SqlTestBase {

        @ExpectMaxSelect(0)
        @Test
        public void execute_one_select_but_no_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = OneSelectButNoSelectExpected.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult)
                .contains("You may think that at most <0> select statement was sent to the database")
                .doesNotContain("N+1 select") // Less than 2 SELECT executed
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id as id1_0_");


    }


    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneSelectButAtMaxOneExpected extends SqlTestBase {

        @ExpectMaxSelect(1)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_not_fail_if_the_performance_property_is_respected() {

        // GIVEN
        Class<?> testClass = OneSelectButAtMaxOneExpected.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneSelectButNoExpectedInSpecificJvm extends SqlTestBase {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectMaxSelect(0)
        @Test
        public void execute_one_select_but_no_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_the_number_expected_and_test_method_annotated_with_xmx() {

        // GIVEN
        Class<?> testClass = OneSelectButNoExpectedInSpecificJvm.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult)
                .contains("You may think that at most <0> select statement was sent to the database")
                .doesNotContain("N+1 select") // Less than 2 SELECT executed
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id as id1_0");

    }


    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoSameSelectTypeWithDifferentParameterValues extends SqlTestBase {

        @ExpectMaxSelect(1)
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
        Class<?> testClass = ExpectSelectTest.TwoSameSelectTypeWithDifferentParameterValues.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that <1> select statement was sent to the database")
                              .contains("server roundtrips")
                              .contains("N+1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoSameSelects extends SqlTestBase {

        @Test
        @ExpectMaxSelect(1)
        public void execute_two_same_selects() {
            EntityManager em = emf.createEntityManager();

            String hqlQuery =   " FROM " + Book.class.getCanonicalName() + " b"
                    + " WHERE b.id=:idParam";

            Query query = em.createQuery(hqlQuery);
            query.setParameter("idParam", 2L);
            query.getResultList();

            Query query2 = em.createQuery(hqlQuery);
            query2.setParameter("idParam", 2L);
            query2.getResultList();

        }

    }

    @Test
    public void
    should_not_display_round_trip_and_n_plus_one_select_message_if_two_same_selects() {

        // GIVEN
        Class<?> testClass = TwoSameSelects.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that at most <1> select statement was sent to the database")
                              .doesNotContain("server roundtrips")
                              .doesNotContain("N+1");

    }

}
