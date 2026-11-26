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
package org.quickperf.junit6;

import org.junit.jupiter.api.Test;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class AutomaticJUnit6ExtensionRegistrationTest {

    static class TestClassNotAnnotatedWithQuickPerfTest {

        @ExpectMaxExecutionTime(milliSeconds = 10)
        @Test
        public void test_during_about_200_ms() throws InterruptedException {
            Thread.sleep(200);
        }

    }

    @Test
    void the_extension_should_be_registered_with_the_autodetection_of_extensions() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(TestClassNotAnnotatedWithQuickPerfTest.class
                , Collections.singletonMap("junit.jupiter.extensions.autodetection.enabled", "true"));

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();
        assertThat(jUnit5TestsResult.getErrorReport()).contains("a performance-related property is not respected");

    }

    @Test
    void the_extension_should_not_be_registered_without_the_autodetection_of_extensions() {

        // GIVEN
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(TestClassNotAnnotatedWithQuickPerfTest.class);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();
        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isOne();

    }

}
