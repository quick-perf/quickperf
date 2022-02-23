package org.quickperf.testng.sql;

import org.quickperf.testng.TestNGTests;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfSqlTestNGListenerTest {

    @Test
    public void a_test_with_failing_business_code_should_fail() {

        // GIVEN
        Class<?> testClass = TestNGMethodFailing.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport).hasMessageContaining("expected [false] but found [true]");

    }

}
