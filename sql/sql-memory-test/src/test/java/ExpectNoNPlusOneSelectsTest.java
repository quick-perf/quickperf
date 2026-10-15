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
import org.quickperf.sql.annotation.AllowNPlusOneSelects;
import org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParamValues;
import org.quickperf.sql.annotation.EnableSameSelectTypesWithDifferentParamValues;
import org.quickperf.sql.annotation.ExpectNoNPlusOneSelects;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

@SuppressWarnings("deprecation")
public class ExpectNoNPlusOneSelectsTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NPlusOneSelects extends SqlTestBase {

        @Test
        @ExpectNoNPlusOneSelects
        public void execute_two_same_select_types_with_two_diff_param_values() {
            executeTwoSameSelectTypesWithTwoDiffParamValues(emf.createEntityManager());
        }

    }

    @Test public void
    should_fail_if_n_plus_one_selects() {

        // GIVEN
        Class<?> testClass = NPlusOneSelects.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult)
                .contains("Same SELECT types with different parameter values")
                .contains("JDBC roundtrips")
                .contains("N+1");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TwoSameSelects extends SqlTestBase {

        @Test
        @ExpectNoNPlusOneSelects
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

    @Test public void
    should_pass_with_two_same_selects() {

        // GIVEN
        Class<?> testClass = TwoSameSelects.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoNPlusOneSelects
    public static class NPlusOneSelectsAllowedOnMethod extends SqlTestBase {

        @Test
        @AllowNPlusOneSelects(comment = "N+1 selects are allowed here")
        public void execute_two_same_select_types_with_two_diff_param_values() {
            executeTwoSameSelectTypesWithTwoDiffParamValues(emf.createEntityManager());
        }

    }

    @Test public void
    should_pass_if_n_plus_one_selects_are_allowed() {

        // GIVEN
        Class<?> testClass = NPlusOneSelectsAllowedOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableSameSelectTypesWithDifferentParamValues
    public static class DeprecatedAnnotationOnClassAndNPlusOneSelectsAllowedOnMethod extends SqlTestBase {

        @Test
        @AllowNPlusOneSelects
        public void execute_two_same_select_types_with_two_diff_param_values() {
            executeTwoSameSelectTypesWithTwoDiffParamValues(emf.createEntityManager());
        }

    }

    @Test public void
    allow_n_plus_one_selects_should_cancel_the_deprecated_disable_same_select_types_with_different_param_values() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndNPlusOneSelectsAllowedOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoNPlusOneSelects
    public static class DeprecatedEnableAnnotationOnMethod extends SqlTestBase {

        @Test
        @EnableSameSelectTypesWithDifferentParamValues
        public void execute_two_same_select_types_with_two_diff_param_values() {
            executeTwoSameSelectTypesWithTwoDiffParamValues(emf.createEntityManager());
        }

    }

    @Test public void
    the_deprecated_enable_same_select_types_with_different_param_values_should_cancel_expect_no_n_plus_one_selects() {

        // GIVEN
        Class<?> testClass = DeprecatedEnableAnnotationOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableSameSelectTypesWithDifferentParamValues
    public static class DeprecatedAnnotationOnClassAndNewAnnotationOnMethod extends SqlTestBase {

        @Test
        @ExpectNoNPlusOneSelects
        public void execute_two_same_select_types_with_two_diff_param_values() {
            executeTwoSameSelectTypesWithTwoDiffParamValues(emf.createEntityManager());
        }

    }

    @Test public void
    should_report_n_plus_one_selects_once_if_the_deprecated_and_the_new_annotations_are_both_applied() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndNewAnnotationOnMethod.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testResult = printableResult.toString();
        assertThat(testResult)
                .containsOnlyOnce("Same SELECT types with different parameter values");

    }

    private static void executeTwoSameSelectTypesWithTwoDiffParamValues(EntityManager em) {

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
