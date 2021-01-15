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
import org.quickperf.sql.annotation.ExpectMaxUpdatedColumn;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExpectMaxUpdatedColumnTest {

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectMaxUpdatedColumn(2)
    public static class AClassAnnotatedWithExpectMaxUpdatedColumnPassing extends SqlTestBase {

        @Test
        public void execute_update_with_two_columns_updated_and_a_where_clause() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title WHERE id = :id")
                               .setParameter("isbn", 12)
                               .setParameter("title", "Manon")
                               .setParameter("id", 40);
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }

    }

    @Test public void
    should_pass_if_the_number_of_sql_updates_is_equal_to_the_number_expected_with_annotation_on_method() {

        // GIVEN
        Class<?> testClass = AClassAnnotatedWithExpectMaxUpdatedColumnPassing.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectMaxUpdatedColumn(1)
    public static class AClassAnnotatedWithExpectMaxUpdatedColumnFailing extends SqlTestBase {

        @Test
        public void execute_update_with_two_columns_updated() {
            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();
            Query nativeQuery = em.createNativeQuery("UPDATE book SET isbn = :isbn, title = :title")
                               .setParameter("isbn", 12)
                               .setParameter("title", "Manon");
            nativeQuery.executeUpdate();
            em.getTransaction().commit();
        }

    }

    @Test public void
    should_fail_if_the_number_of_sql_updates_is_greater_than_the_number_expected_with_annotation_on_method() {

        // GIVEN
        Class<?> testClass = AClassAnnotatedWithExpectMaxUpdatedColumnFailing.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString()).contains(
                "Maximum expected number of updated columns <1> but is <2>.");

        assertThat(printableResult.toString().toLowerCase())
                      .contains("update")
                      .contains("book")
                      .contains("set")
                      .contains("isbn = ?")
                      .contains("title = ?");

    }

}
