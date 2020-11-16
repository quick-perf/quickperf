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
import org.quickperf.sql.annotation.ExpectMaxUpdate;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExpectMaxUpdateTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectMaxUpdate extends SqlTestBase {

        @Test
        @ExpectMaxUpdate(0)
        public void execute_one_update_but_no_update_expected() {

            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title WHERE id = :id")
                               .setParameter("isbn", 42)
                               .setParameter("title", "Tristan")
                               .setParameter("id", 40);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();

        }

    }

    @Test
    public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_the_number_expected_with_annotation_on_method() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxUpdate.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("You may think that at most <0> update statement")
                .contains("But there is in fact <1>");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectMaxUpdate(0)
    public static class AClassAnnotatedWithExpectMaxUpdate extends SqlTestBase {

        @Test
        public void execute_one_update_but_no_update_expected() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title WHERE id = :id")
                               .setParameter("isbn", 42)
                               .setParameter("title", "Tristan")
                               .setParameter("id", 40);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }

    }

    @Test
    public void
    should_fail_if_the_number_of_sql_statements_is_greater_than_the_number_expected_with_annotation_on_class() {

        // GIVEN
        Class<?> testClass = AClassAnnotatedWithExpectMaxUpdate.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("You may think that at most <0> update statement was sent to the database")
                .contains("But there is in fact <1>...");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithExpectMaxUpdateAndHavingNoPerfIssue extends SqlTestBase {

        @ExpectMaxUpdate(1)
        @Test
        public void execute_one_update() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title WHERE id = :id")
                               .setParameter("isbn", 42)
                               .setParameter("title", "Tristan")
                               .setParameter("id", 40);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }

    }

    @Test
    public void
    should_not_fail_if_the_performance_property_is_respected() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithExpectMaxUpdateAndHavingNoPerfIssue.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
