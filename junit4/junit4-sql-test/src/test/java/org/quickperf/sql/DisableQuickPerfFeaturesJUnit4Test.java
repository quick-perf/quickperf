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
package org.quickperf.sql;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectSelects;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class DisableQuickPerfFeaturesJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassWithAMethodAnnotatedWithDisableQuickPerf extends SqlTestBaseJUnit4 {

        @DisableQuickPerf
        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

        @DisableQuickPerf
        @ExpectSelects({
                @ExpectSelect(comment = "Select books"),
                @ExpectSelect(comment = "Select related entities", value = 4)
        })
        @Test
        public void execute_one_select_but_five_select_expected_with_repeated_annotations() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    disable_quick_perf_annotation_should_disable_quick_perf() {

        // GIVEN
        Class<?> testClass = AClassWithAMethodAnnotatedWithDisableQuickPerf.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassWithAMethodAnnotatedWithFunctionalIteration extends SqlTestBaseJUnit4 {

        @FunctionalIteration
        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

        @FunctionalIteration
        @ExpectSelects({
                @ExpectSelect(comment = "Select books"),
                @ExpectSelect(comment = "Select related entities", value = 4)
        })
        @Test
        public void execute_one_select_but_five_select_expected_with_repeated_annotations() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    functional_iteration_annotation_should_disable_quick_perf() {

        // GIVEN
        Class<?> testClass = AClassWithAMethodAnnotatedWithFunctionalIteration.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
