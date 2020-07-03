/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.time;

import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;

import java.util.concurrent.TimeUnit;

public class MaxExecutionTimeVerifier implements VerifiablePerformanceIssue<ExpectMaxExecutionTime, ExecutionTime> {

    public static final MaxExecutionTimeVerifier INSTANCE = new MaxExecutionTimeVerifier();

    private final ExecutionTimeFormatter executionTimeFormatter = ExecutionTimeFormatter.INSTANCE;

    private final ExpectMaxExecutionTimeAnnotationFormatter annotationFormatter = ExpectMaxExecutionTimeAnnotationFormatter.INSTANCE;

    private MaxExecutionTimeVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxExecutionTime annotation, ExecutionTime measuredExecutionTime) {

        ExecutionTime maxExpectedExecutionTime = buildMaxExpectedExecutionTimeFrom(annotation);

        if(measuredExecutionTime.isGreaterThan(maxExpectedExecutionTime)) {

            String description =
                      "Execution time of test method expected to be less than"
                    + " " + "<" + annotationFormatter.format(annotation) + ">"
                    + " but is " + "<" + executionTimeFormatter.formatAndAppendNanoSeconds(measuredExecutionTime) + ">"
                    + System.lineSeparator()
                    + ExecutionTimeWarning.INSTANCE.toString()
                    ;
            
            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }

    private ExecutionTime buildMaxExpectedExecutionTimeFrom(ExpectMaxExecutionTime annotation) {
        long maxExpectedExecutionTimeInNanoSeconds =
                  TimeUnit.HOURS.toNanos(annotation.hours())
                + TimeUnit.MINUTES.toNanos(annotation.minutes())
                + TimeUnit.SECONDS.toNanos(annotation.seconds())
                + TimeUnit.MILLISECONDS.toNanos(annotation.milliSeconds())
                + TimeUnit.MICROSECONDS.toNanos(annotation.microSeconds())
                + annotation.nanoSeconds();
        return new ExecutionTime(maxExpectedExecutionTimeInNanoSeconds
                               , TimeUnit.NANOSECONDS);
    }

}
