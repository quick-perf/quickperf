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

package org.quickperf.jvm.jfr;

import org.openjdk.jmc.common.item.IItemCollection;
import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.jvm.jfr.profiler.JdkFlightRecorderProfilerFactory;
import org.quickperf.jvm.jfr.profiler.JvmProfiler;
import org.quickperf.perfrecording.RecordablePerformance;

public class JfrEventsRecorder implements RecordablePerformance<JfrRecording> {

    private final JvmProfiler jfrProfiler = JdkFlightRecorderProfilerFactory.getJdkFlightRecorderProfiler();

    private JfrRecording jfrRecording;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        jfrProfiler.startProfiling(workingFolder.getPath());
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        jfrProfiler.stopProfiling(workingFolder.getPath());
    }

    @Override
    public JfrRecording findRecord(TestExecutionContext testExecutionContext) {
        if (jfrRecording != null) {
            return jfrRecording;
        }
        JFREventsLoader jfrEventsLoader = new JFREventsLoader();
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        IItemCollection jfrEvents = jfrEventsLoader.loadJfrEventsFrom(workingFolder);
        jfrRecording = new JfrRecording(jfrEvents);
        return jfrRecording;
    }

    @Override
    public void cleanResources() { }

}
