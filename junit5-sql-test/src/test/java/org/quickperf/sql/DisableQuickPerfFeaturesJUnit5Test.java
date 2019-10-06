package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class DisableQuickPerfFeaturesJUnit5Test {

    @QuickPerfTest
    public static class AClassWithAMethodAnnotatedWithDisableQuickPerf extends SqlJUnit5TestBase {

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
    disable_quick_perf_annotation_should_disable_quick_perf() {

        // GIVEN
        LauncherDiscoveryRequest request =
                         request()
                        .selectors(selectClass(AClassWithAMethodAnnotatedWithDisableQuickPerf.class))
                        .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();
        long testsFailedCount = testExecutionSummary.getTestsFailedCount();
        assertThat(testsFailedCount).isEqualTo(0);

    }

    @QuickPerfTest
    public static class AClassWithAMethodAnnotatedWithFunctionalIteration extends SqlJUnit5TestBase {

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
    functional_iteration_annotation_should_disable_quick_perf() {

        // GIVEN
        LauncherDiscoveryRequest request =
                         request()
                        .selectors(selectClass(AClassWithAMethodAnnotatedWithFunctionalIteration.class))
                        .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();
        long testsFailedCount = testExecutionSummary.getTestsFailedCount();
        assertThat(testsFailedCount).isEqualTo(0);

    }

}
