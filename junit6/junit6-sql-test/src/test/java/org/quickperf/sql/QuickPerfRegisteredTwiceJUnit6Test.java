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

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.junit6.QuickPerfTestExtension;
import org.quickperf.sql.annotation.ExpectSelect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.Selects.executeASelect;

// JUnit registers the instance of a @RegisterExtension field even if @QuickPerfTest has registered the extension. The
// first instance records the test, and the other one only proceeds. quick-perf-junit5 measures such a test twice.
class QuickPerfRegisteredTwiceJUnit6Test {

    @QuickPerfTest
    static class TestsWithQuickPerfRegisteredTwice {

        @RegisterExtension
        static QuickPerfTestExtension quickPerfTestExtension = new QuickPerfTestExtension();

        @ExpectSelect(2)
        @Test
        void a_test_executing_one_select() {
            executeASelect();
        }

        @ExpectSelect(2)
        @RepeatedTest(2)
        void a_repeated_test_executing_one_select() {
            executeASelect();
        }

    }

    @Test
    void should_measure_and_report_each_test_once() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestsWithQuickPerfRegisteredTwice.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).as(errorReport).isEqualTo(3);
        assertThat(errorReport).doesNotContain("Performance-related and functional properties not respected");
        assertThat(occurrencesIn(errorReport, "You may think that <2> select statements were sent to the database"))
                .as("reported performance issues.%s", errorReport)
                .isEqualTo(3);

    }

    private static int occurrencesIn(String text, String searched) {
        int occurrences = 0;
        for (int index = text.indexOf(searched); index >= 0; index = text.indexOf(searched, index + 1)) {
            occurrences++;
        }
        return occurrences;
    }

}
