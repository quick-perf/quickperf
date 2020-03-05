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
 * Copyright 2019-2019 the original author or authors.
 */

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParamValues;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class DisableSameSelectTypesWithDifferentParamValuesTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithDisableSameSelectTypeWithDifferentParams extends SqlTestBase {

        @Test
        @DisableSameSelectTypesWithDifferentParamValues
        public void execute_two_same_select_types_with_two_diff_params() {

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
    should_fail_if_two_selects_with_diff_params_and_test_annotated_with_disable_select_with_diff_params() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithDisableSameSelectTypeWithDifferentParams.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Same SELECT types with different parameter values");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingASameParamsMethodAnnotatedWithDisableSameSelectTypeWithDifferentParams extends SqlTestBase {

        @Test
        @DisableSameSelectTypesWithDifferentParamValues
        public void execute_two_same_select_with_two_same_params() {
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
    should_pass_with_two_selects_with_same_params_and_test_annotated_with_disable_select_with_diff_params() {

        // GIVEN
        Class<?> testClass = AClassHavingASameParamsMethodAnnotatedWithDisableSameSelectTypeWithDifferentParams.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
