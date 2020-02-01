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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.perfrecording;

import org.quickperf.TestExecutionContext;

import java.util.List;

public class PerformanceRecording {

    public static final PerformanceRecording INSTANCE = new PerformanceRecording();

    private PerformanceRecording() {}

    public void start(TestExecutionContext testExecutionContext) {
        List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod = testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod();
        int numberOfPerfRecordersToExecuteBeforeTestMethod = perfRecordersToExecuteBeforeTestMethod.size();
        for (int i = 0; i < numberOfPerfRecordersToExecuteBeforeTestMethod; i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteBeforeTestMethod.get(i);
            recordablePerformance.startRecording(testExecutionContext);
        }
    }

    public void stop(TestExecutionContext testExecutionContext) {
        List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
        for (int i = 0; i < perfRecordersToExecuteAfterTestMethod.size() ; i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteAfterTestMethod.get(i);
            recordablePerformance.stopRecording(testExecutionContext);
        }
    }

}
