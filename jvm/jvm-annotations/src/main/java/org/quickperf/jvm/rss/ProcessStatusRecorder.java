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

package org.quickperf.jvm.rss;

import org.quickperf.TestExecutionContext;
import org.quickperf.perfrecording.RecordablePerformance;

public class ProcessStatusRecorder implements RecordablePerformance<ProcessStatus> {
    ProcessStatusRepository processStatusRepository = new ProcessStatusRepository();

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        // nothing here, recording is done when stoping the recorder
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        ProcessStatus.record();
        processStatusRepository.save(ProcessStatus.getRecord(), testExecutionContext);
    }

    @Override
    public ProcessStatus findRecord(TestExecutionContext testExecutionContext) {
        processStatusRepository = new ProcessStatusRepository();
        return processStatusRepository.find(testExecutionContext);
    }

    @Override
    public void cleanResources() {
        ProcessStatus.reset();
    }
}
