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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class QuickPerfParallelExecutionTest {

    @QuickPerfTest
    static class ParallelSqlTests extends SqlTestBaseJUnit5 {

        @BeforeEach
        void widenRaceWindow() throws InterruptedException {
            // Widen the window between extension.beforeEach()
            // (writes testExecutionContext) and extension.interceptTestMethod()
            // (reads testExecutionContext).
            Thread.sleep(100);
        }

        // Methods that execute 1 SELECT

        @Test @ExpectSelect(1) void select_a() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_b() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_c() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_d() { executeOneSelect(); }
        @Test @ExpectSelect(1) void select_e() { executeOneSelect(); }

        // Methods that execute 0 SELECT

        @Test @ExpectSelect(0) void no_select_a() { }
        @Test @ExpectSelect(0) void no_select_b() { }
        @Test @ExpectSelect(0) void no_select_c() { }
        @Test @ExpectSelect(0) void no_select_d() { }
        @Test @ExpectSelect(0) void no_select_e() { }

        private void executeOneSelect() {
            EntityManager em = emf.createEntityManager();
            em.createQuery("FROM " + Book.class.getCanonicalName()).getResultList();
        }
    }

    @Test
    void parallel_execution_should_not_corrupt_sql_test_contexts() {

        // GIVEN
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(ParallelSqlTests.class))
                .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
                .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
                .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "10")
                .build();

        // WHEN
        Launcher launcher = LauncherFactory.create();
        launcher.execute(request, listener);
        TestExecutionSummary summary = listener.getSummary();

        // THEN
        assertThat(summary.getTestsSucceededCount()).isEqualTo(10);
        assertThat(summary.getTestsFailedCount()).isZero();
    }
}
