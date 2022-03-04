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
package org.quickperf.junit5;

import org.junit.jupiter.api.Test;
import org.quickperf.annotation.ExpectMaxExecutionTime;

import static org.assertj.core.api.Assertions.assertThat;

public class AutomaticJunit5ExtensionRegistrationTest {

    public static class TestClassNotAnnotatedWithQuickPerfTest {

        @ExpectMaxExecutionTime(milliSeconds = 100)
        @Test
        public void test_during_about_one_second() throws InterruptedException {
            Thread.sleep(1_000L);
        }

    }

    @Test public void
    quick_perf_extension_should_be_automatically_registered_because_automatic_extension_registration_is_enabled_in_junit_platform_properties_file() {

        // GIVEN
        Class<?> testClass = TestClassNotAnnotatedWithQuickPerfTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

    }

}
