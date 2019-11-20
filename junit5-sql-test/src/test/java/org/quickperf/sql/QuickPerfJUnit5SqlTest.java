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

package org.quickperf.sql;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class QuickPerfJUnit5SqlTest {

    private final JUnit5FailuresFormatter jUnit5FailuresFormatter = JUnit5FailuresFormatter.INSTANCE;

    @QuickPerfTest
    public static class SqlSelectJUnit5 extends SqlJUnit5TestBase {

        @ExpectSelect(5)
        @Test
        public void execute_one_select_but_five_select_expected() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }

    @Test public void
    should_fail_if_a_sql_performance_property_is_un_respected() {

        // GIVEN
        LauncherDiscoveryRequest request =
                 request()
                .selectors(selectClass(SqlSelectJUnit5.class))
                .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();

        SoftAssertions softAssertions = new SoftAssertions();

        long testsFailedCount = testExecutionSummary.getTestsFailedCount();
        softAssertions.assertThat(testsFailedCount).isEqualTo(1);

        String testExecutionSummaryAsString = jUnit5FailuresFormatter.formatToStringFrom(testExecutionSummary);
        softAssertions.assertThat(testExecutionSummaryAsString).contains("You may think that <5> select statements were sent to the database");
        softAssertions.assertThat(testExecutionSummaryAsString).contains("But in fact <1>...");

        softAssertions.assertThat(testExecutionSummaryAsString)
                      .contains("select")
                      .contains("book0_.id")
                      .contains("from")
                      .contains("Book book0_");

        softAssertions.assertAll();

    }


}
