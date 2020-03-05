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

package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class DisableQuickPerfFeaturesJUnit5Test {

    @QuickPerfTest
    public static class AClassWithAMethodAnnotatedWithDisableQuickPerf extends SqlTestBaseJUnit5 {

        @DisableQuickPerf
        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    disable_quick_perf_annotation_should_disable_quick_perf_features() {

        // GIVEN
        Class<?> testClass = AClassWithAMethodAnnotatedWithDisableQuickPerf.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();

    }

    @QuickPerfTest
    public static class AClassWithAMethodAnnotatedWithFunctionalIteration extends SqlTestBaseJUnit5 {

        @FunctionalIteration
        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    functional_iteration_annotation_should_disable_quick_perf_features() {

        // GIVEN

        Class<?> testClass = AClassWithAMethodAnnotatedWithFunctionalIteration.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();

    }

}
