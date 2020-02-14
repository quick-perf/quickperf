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

package org.quickperf.jvm.rss;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxRSS;
import org.quickperf.jvm.annotations.MeasureRSS;

import java.util.Locale;

import static org.junit.Assume.assumeFalse;
import static org.junit.experimental.results.PrintableResult.testResult;

public class JUnit4RssTests {

    @Before
    public void before() {
        notWindows();
        notMacOS();
    }

    private void notWindows() {
        String osName = extractOSNameInLowerCase();
        boolean onWindows = osName.contains("win");
        assumeFalse(onWindows);
    }

    private void notMacOS() {
        String osName = extractOSNameInLowerCase();
        boolean onMac = osName.contains("mac");
        assumeFalse(onMac);
    }

    private String extractOSNameInLowerCase() {
        String osName = System.getProperty("os.name");
        osName = osName.toLowerCase(Locale.ENGLISH);
        return osName;
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassWithRssAnnotations {

        @MeasureRSS
        @ExpectMaxRSS(value=10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void measure_and_expect_rss() {
        }
    }

    @Test
    public void
    rss_measure_expecting_10m() {

        // GIVEN
        Class<?> testClass = ClassWithRssAnnotations.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount())
                .isEqualTo(0);
        softAssertions.assertAll();

    }
}
