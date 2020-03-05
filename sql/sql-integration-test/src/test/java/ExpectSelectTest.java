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

        assertThat(printableResult.toString())
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
    should_not_display_round_trip_and_n_plus_one_select_messages_if_select_number_less_than_expected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectSelectAndSelectsLessThanExpected.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.toString()).doesNotContain("server roundtrips")
                                              .doesNotContain("N+1");

    }

}
