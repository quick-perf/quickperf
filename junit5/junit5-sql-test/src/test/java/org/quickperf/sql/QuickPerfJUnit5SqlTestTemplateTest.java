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

package org.quickperf.sql;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test uses @RepeatedTest to test the Test template support as repeated tests are built-in test templates.
 */
public class QuickPerfJUnit5SqlTestTemplateTest {

    @QuickPerfTest
    public static class SqlSelectJUnit5 extends SqlTestBaseJUnit5 {

        @ExpectSelect(2)
        @RepeatedTest(2)
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected() {

        // GIVEN
        Class<?> testClass = SqlSelectJUnit5.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(2);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <2> select statements were sent to the database")
                               .contains("But there is in fact <1>...")
                               .contains("select")
                               .contains("book0_.id")
                               .contains("from")
                               .contains("Book book0_");

    }

}
