package org.quickperf.junit5;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class JUnit5ExtensionTest {

    @QuickPerfTest
    public static class ClassWithAMethodHavingFailingBusinessCode {

        @Test
        public void
        a_test_with_failing_business_code() {
            assertThat(false).isTrue();
        }

    }

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        LauncherDiscoveryRequest request =
                         request()
                        .selectors(selectClass(ClassWithAMethodHavingFailingBusinessCode.class))
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

}
