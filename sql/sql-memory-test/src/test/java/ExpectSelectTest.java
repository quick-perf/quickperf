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
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectSelectTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectSelect extends SqlTestBase {

        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_the_number_of_select_statements_is_not_equal_to_the_number_expected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectSelect.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult)
                .contains("You may think that <5> select statements were sent to the database")
                .contains("But in fact <1>...")
                .contains("select")
                .contains("book0_.id as id1_0");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectSelectAndSelectsLessThanExpected extends SqlTestBase {

        @ExpectSelect(2)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_not_display_round_trip_and_n_plus_one_select_message_if_a_select_number_less_than_expected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectSelectAndSelectsLessThanExpected.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.toString()).doesNotContain("server roundtrips")
                                              .doesNotContain("N+1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class OneSelectAndExpectZero extends SqlTestBase {

        @ExpectSelect(0)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_not_display_round_trip_and_n_plus_one_select_message_if_less_than_two_selects() {

        // GIVEN
        Class<?> testClass = OneSelectAndExpectZero.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that <0> select statement was sent to the database")
                              .doesNotContain("server roundtrips")
                              .doesNotContain("N+1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoSameSelectTypeWithDifferentParameterValues extends SqlTestBase {

        @ExpectSelect(1)
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
        assertThat(testResult).contains("You may think that <1> select statement was sent to the database")
                              .contains("server roundtrips")
                              .contains("N+1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoDifferentSelectTypes extends SqlTestBase {

        @ExpectSelect(1)
        @Test
        public void execute_two_different_select_types() {

            EntityManager em = emf.createEntityManager();

            String idParamName = "idParam";
            Query firstSelectType = em.createQuery(
                        "FROM " + Book.class.getCanonicalName()
                            + " b WHERE b.id=:" + idParamName
                                                  );
            firstSelectType.setParameter(idParamName, 2L);
            firstSelectType.getResultList();

            String isbnParamName = "isbnParam";
            Query secondSelectType = em.createQuery("FROM " + Book.class.getCanonicalName() + " b WHERE b.isbn=:" + isbnParamName);
            secondSelectType.setParameter(isbnParamName, "anIsbn");
            secondSelectType.getResultList();

        }

    }

    @Test public void
    should_not_display_round_trip_and_n_plus_one_select_message_if_two_different_select_types() {

        // GIVEN
        Class<TwoDifferentSelectTypes> testClass = TwoDifferentSelectTypes.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult).contains("You may think that <1> select statement was sent to the database")
                              .doesNotContain("server roundtrips")
                              .doesNotContain("N+1");

    }

}
