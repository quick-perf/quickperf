/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.time;

import org.junit.Test;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.issue.PerfIssue;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxExecutionTimeVerifierTest {

    private int hours;
    private int minutes;
    private int seconds;
    private int milliSeconds;

    private final ExpectMaxExecutionTime annotation = new ExpectMaxExecutionTime() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return ExpectMaxExecutionTime.class;
        }
        @Override
        public int hours() {
            return hours;
        }
        @Override
        public int minutes() {
            return minutes;
        }
        @Override
        public int seconds() {
            return seconds;
        }
        @Override
        public int milliSeconds() {
            return milliSeconds;
        }
    };

    @Test
    public void should_return_a_perf_issue_if_execution_time_is_greater_than_expected() {

        MaxExecutionTimeVerifier maxExecutionTimeVerifier = MaxExecutionTimeVerifier.INSTANCE;

        ExecutionTime measuredExecutionTime = new ExecutionTime(188
                                                              , TimeUnit.MINUTES);

        hours = 2;
        minutes = 3;
        seconds = 4;
        milliSeconds = 5;

        PerfIssue perfIssue = maxExecutionTimeVerifier.verifyPerfIssue(annotation, measuredExecutionTime);

        assertThat(perfIssue).isNotEqualTo(PerfIssue.NONE);

        String description = perfIssue.getDescription();
        assertThat(description).startsWith("Execution time of the test method expected to be less than <2 h 3 m 4 s 5 ms> but is <3 h 8 m 0 s (11 280 000 000 000 ns)>");

    }

}