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
package org.quickperf.junit5.jvm.issue;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.annotations.JvmOptions;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassPathJarThresholdTest {

    @QuickPerfTest
    static class ClassUnderTest {

        @JvmOptions(value = "-Dquickperf.classPathJarThreshold=1")
        @Test
        public void a_test() {
        }

    }

    @Test public void
    should_not_fail_when_using_jar_class_path() {

        // GIVEN
        Class<?> testClass = ClassUnderTest.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();

    }

}
