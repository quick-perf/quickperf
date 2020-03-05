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

package org.quickperf.perfrecording;

import org.quickperf.TestExecutionContext;

public interface RecordablePerformance<R extends PerfRecord> {

    RecordablePerformance NONE = new RecordablePerformance() {

        @Override
        public void startRecording(TestExecutionContext testExecutionContext) {}

        @Override
        public void stopRecording(TestExecutionContext testExecutionContext) { }

        @Override
        public PerfRecord findRecord(TestExecutionContext testExecutionContext) {
            return PerfRecord.NONE.NONE;
        }

        @Override
        public void cleanResources() { }

        @Override
        public String toString() {
            return "No performance recording.";
        }

    };

    void startRecording(TestExecutionContext testExecutionContext);

    void stopRecording(TestExecutionContext testExecutionContext);

    R findRecord(TestExecutionContext testExecutionContext);

    void cleanResources();
}
