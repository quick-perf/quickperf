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
package org.quickperf.time;

import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;

public class MeasureExecutionTimeReporter implements VerifiablePerformanceIssue<MeasureExecutionTime, ExecutionTime>  {

    public static final MeasureExecutionTimeReporter INSTANCE = new MeasureExecutionTimeReporter();

    private MeasureExecutionTimeReporter() {}

    private final ExecutionTimeFormatter formatter = ExecutionTimeFormatter.INSTANCE;

    @Override
    public PerfIssue verifyPerfIssue(MeasureExecutionTime annotation, ExecutionTime measuredExecutionTime) {
        System.out.println("[QUICK PERF] Execution time of the test method: " + formatter.formatAndAppendNanoSeconds(measuredExecutionTime)
                          + System.lineSeparator()
                          + ExecutionTimeWarning.INSTANCE.toString()
                          );
        return PerfIssue.NONE;
    }

}
