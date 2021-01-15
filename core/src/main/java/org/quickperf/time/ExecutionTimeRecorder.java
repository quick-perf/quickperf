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

import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.repository.LongFileRepository;

import java.util.concurrent.TimeUnit;

public class ExecutionTimeRecorder implements RecordablePerformance<ExecutionTime> {

    private static final String EXECUTION_TIME_FILE_NAME = "execution-time.ser";

    private long startTimeInNanoSeconds;

    private long executionTimeInNanoSeconds;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        startTimeInNanoSeconds = System.nanoTime();
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {

        executionTimeInNanoSeconds = System.nanoTime() - startTimeInNanoSeconds;

        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            String workingFolderPath = findWorkingFolderPathFrom(testExecutionContext);
            LongFileRepository longFileRepository = new LongFileRepository();
            longFileRepository.save( executionTimeInNanoSeconds
                                   , workingFolderPath
                                   , EXECUTION_TIME_FILE_NAME );
        }

    }

    private String findWorkingFolderPathFrom(TestExecutionContext testExecutionContext) {
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        return workingFolder.getPath();
    }

    @Override
    public ExecutionTime findRecord(TestExecutionContext testExecutionContext) {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            LongFileRepository longFileRepository = new LongFileRepository();
            String workingFolderPath = findWorkingFolderPathFrom(testExecutionContext);
            Long executionTimeInNanoSeconds = longFileRepository.find(workingFolderPath, EXECUTION_TIME_FILE_NAME);
            return new ExecutionTime(executionTimeInNanoSeconds, TimeUnit.NANOSECONDS);
        }
        return new ExecutionTime(executionTimeInNanoSeconds, TimeUnit.NANOSECONDS);
    }

    @Override
    public void cleanResources() {
    }

}
