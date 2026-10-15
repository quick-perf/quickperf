/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2022 the original author or authors.
 */
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.AllowDuplicateSelects;
import org.quickperf.sql.annotation.DisableSameSelects;
import org.quickperf.sql.annotation.EnableSameSelects;
import org.quickperf.sql.annotation.ExpectNoDuplicateSelects;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

@SuppressWarnings("deprecation")
public class ExpectNoDuplicateSelectsTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DuplicateSelects extends SqlTestBase {

        @Test
        @ExpectNoDuplicateSelects
        public void execute_two_same_selects_with_same_params() {
            executeTwoSameSelectsWithSameParams(emf.createEntityManager());
        }

    }

    @Test public void
    should_fail_if_duplicate_selects() {

        // GIVEN
        Class<?> testClass = DuplicateSelects.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("[PERF] Same SELECT statements");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SameSelectTypesWithDifferentParamValues extends SqlTestBase {

        @Test
        @ExpectNoDuplicateSelects
        public void execute_two_same_select_types_with_different_param_values() {
            EntityManager em = emf.createEntityManager();

            String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id = :idParam";

            Query query1 = em.createQuery(hqlQuery);
            query1.setParameter("idParam", 3L);
            query1.getResultList();

            Query query2 = em.createQuery(hqlQuery);
            query2.setParameter("idParam", 1L);
            query2.getResultList();

        }

    }

    @Test public void
    should_pass_with_same_select_types_with_different_param_values() {

        // GIVEN
        Class<?> testClass = SameSelectTypesWithDifferentParamValues.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoDuplicateSelects
    public static class DuplicateSelectsAllowedOnMethod extends SqlTestBase {

        @Test
        @AllowDuplicateSelects(comment = "Duplicate selects are allowed here")
        public void execute_two_same_selects_with_same_params() {
            executeTwoSameSelectsWithSameParams(emf.createEntityManager());
        }

    }

    @Test public void
    should_pass_if_duplicate_selects_are_allowed() {

        // GIVEN
        Class<?> testClass = DuplicateSelectsAllowedOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableSameSelects
    public static class DeprecatedAnnotationOnClassAndDuplicateSelectsAllowedOnMethod extends SqlTestBase {

        @Test
        @AllowDuplicateSelects
        public void execute_two_same_selects_with_same_params() {
            executeTwoSameSelectsWithSameParams(emf.createEntityManager());
        }

    }

    @Test public void
    allow_duplicate_selects_should_cancel_the_deprecated_disable_same_selects() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndDuplicateSelectsAllowedOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoDuplicateSelects
    public static class DeprecatedEnableAnnotationOnMethod extends SqlTestBase {

        @Test
        @EnableSameSelects
        public void execute_two_same_selects_with_same_params() {
            executeTwoSameSelectsWithSameParams(emf.createEntityManager());
        }

    }

    @Test public void
    the_deprecated_enable_same_selects_should_cancel_expect_no_duplicate_selects() {

        // GIVEN
        Class<?> testClass = DeprecatedEnableAnnotationOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableSameSelects
    public static class DeprecatedAnnotationOnClassAndNewAnnotationOnMethod extends SqlTestBase {

        @Test
        @ExpectNoDuplicateSelects
        public void execute_two_same_selects_with_same_params() {
            executeTwoSameSelectsWithSameParams(emf.createEntityManager());
        }

    }

    @Test public void
    should_report_duplicate_selects_once_if_the_deprecated_and_the_new_annotations_are_both_applied() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndNewAnnotationOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .containsOnlyOnce("[PERF] Same SELECT statements");

    }

    private static void executeTwoSameSelectsWithSameParams(EntityManager em) {

        String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id = :idParam";

        Query query1 = em.createQuery(hqlQuery);
        query1.setParameter("idParam", 3L);
        query1.getResultList();

        Query query2 = em.createQuery(hqlQuery);
        query2.setParameter("idParam", 3L);
        query2.getResultList();

    }

}
