package org.quickperf.junit5;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class JUnit5ExtensionTest {

    @QuickPerfTest
    public static class JUnit5MethodFailing {

        @Test
        public void a_failing_test() {
            assertThat(false).isTrue();
        }

    }

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        LauncherDiscoveryRequest request =
                         request()
                        .selectors(selectClass(JUnit5MethodFailing.class))
                        .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();

        SoftAssertions softAssertions = new SoftAssertions();

        long testsFailedCount = testExecutionSummary.getTestsFailedCount();
        softAssertions.assertThat(testsFailedCount).isEqualTo(1);

        String testExecutionSummaryAsString = formatToStringFrom(testExecutionSummary);
        softAssertions.assertThat(testExecutionSummaryAsString)
                      .contains("Expecting:")
                      .contains("<false>")
                      .contains("to be equal to:")
                      .contains("<true>")
        ;

        softAssertions.assertAll();

    }

    public String formatToStringFrom(TestExecutionSummary testExecutionSummary) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        testExecutionSummary.printFailuresTo(printWriter);
        return stringWriter.getBuffer().toString();
    }


    @QuickPerfTest
    public static class JUnit5MethodFailingInASpecificJvm {

        @HeapSize(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void a_failing_test() {
            assertThat(false).isTrue();
        }

    }

    @Test public void
    a_test_with_failing_business_code_should_fail_when_the_test_method_is_executed_in_a_specific_jvm() {

        // GIVEN
        LauncherDiscoveryRequest request =
                         request()
                        .selectors(selectClass(JUnit5MethodFailingInASpecificJvm.class))
                        .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();

        // WHEN
        LauncherFactory.create().execute(request, summary);

        // THEN
        TestExecutionSummary testExecutionSummary = summary.getSummary();

        SoftAssertions softAssertions = new SoftAssertions();

        long testsFailedCount = testExecutionSummary.getTestsFailedCount();
        softAssertions.assertThat(testsFailedCount).isEqualTo(1);

        String testExecutionSummaryAsString = formatToStringFrom(testExecutionSummary);
        softAssertions.assertThat(testExecutionSummaryAsString)
                .contains("Expecting:")
                .contains("<false>")
                .contains("to be equal to:")
                .contains("<true>")
        ;

        softAssertions.assertAll();

    }


}
