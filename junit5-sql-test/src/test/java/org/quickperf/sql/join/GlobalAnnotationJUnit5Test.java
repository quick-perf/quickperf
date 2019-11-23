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

package org.quickperf.sql.join;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.JUnit5FailuresFormatter;
import org.quickperf.sql.SqlTestBaseJUnit5;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class GlobalAnnotationJUnit5Test {

    private final JUnit5FailuresFormatter jUnit5FailuresFormatter = JUnit5FailuresFormatter.INSTANCE;

    @QuickPerfTest
    public static class SqlCrossJoinJUnit5 extends SqlTestBaseJUnit5 {

        @Test
        public void fail_to_execute_cross_join() {
            EntityManager entityManager = emf.createEntityManager();
            String nativeQuery = "SELECT b1.* FROM Book b1 CROSS JOIN Book b2";
            Query query = entityManager.createNativeQuery(nativeQuery);
            query.getResultList();
        }

    }

    @Test
    public void should_apply_global_annotation() {

        // GIVEN
        LauncherDiscoveryRequest request =
                request()
                        .selectors(selectClass(SqlCrossJoinJUnit5.class))
                        .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();
        String testExecutionSummaryAsString = jUnit5FailuresFormatter.formatToStringFrom(testExecutionSummary);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(testExecutionSummaryAsString)
                .contains("cross join detected")
                .contains("CROSS JOIN") //query cross join
        ;
        softAssertions.assertAll();
    }
}
