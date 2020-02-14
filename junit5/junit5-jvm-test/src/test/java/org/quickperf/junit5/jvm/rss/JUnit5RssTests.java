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
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.junit5.jvm.rss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxRSS;
import org.quickperf.jvm.annotations.MeasureRSS;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs({OS.WINDOWS, OS.MAC})
public class JUnit5RssTests {

    @QuickPerfTest
    public static class ClassWithRssAnnotations {

        @MeasureRSS
        @ExpectMaxRSS(value=10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void measure_and_expect_rss() {
        }

    }

    @Test public void
    rss_measure_expecting_10m() {

        // GIVEN
        Class<?> testClass = ClassWithRssAnnotations.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(0);

    }

}
