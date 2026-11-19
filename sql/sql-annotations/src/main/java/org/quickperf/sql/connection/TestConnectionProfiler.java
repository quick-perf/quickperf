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
package org.quickperf.sql.connection;

import org.quickperf.TestExecutionContext;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.perfrecording.ThreadBoundPerfRecorder;

public class TestConnectionProfiler implements RecordablePerformance<BooleanMeasure>, ThreadBoundPerfRecorder {

    private final ConnectionEventsProfiler connectionEventsProfiler;

    // True while the profiler is also a listener of the recording thread,
    // because another thread registered it. Only used by the recording thread.
    private boolean addedToRecordingThread;

    public TestConnectionProfiler(AnnotationProfilingParameters annotationProfilingParameters) {
        ProfilingParameters profilingParameters = annotationProfilingParameters.getProfilingParameters();
        connectionEventsProfiler = new ConnectionEventsProfiler(profilingParameters);
        if(annotationProfilingParameters.isBeforeAndAfterTestMethodExecution()) {
            connectionEventsProfiler.start();
        }
        ConnectionListenerRegistry.INSTANCE.register(connectionEventsProfiler);
    }

    // The code executed before and after the test method may be executed on
    // another thread than the one that registered the profiler (JUnit 4
    // Timeout rule). cleanResources(), on the same thread, unbinds it.
    @Override
    public void bindToCurrentThread() {
        ConnectionListenerRegistry.INSTANCE.addToListenersOfCurrentThread(connectionEventsProfiler);
    }

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        // The test method may be executed on another thread than the one that
        // registered the profiler (JUnit 4 timeout, JUnit 5 @Timeout on a
        // separate thread): make it a listener of the recording thread too.
        addedToRecordingThread = ConnectionListenerRegistry.INSTANCE.addToListenersOfCurrentThread(connectionEventsProfiler);
        connectionEventsProfiler.start();
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        connectionEventsProfiler.stop();
        if (addedToRecordingThread) {
            ConnectionListenerRegistry.INSTANCE.removeFromListenersOfCurrentThread(connectionEventsProfiler);
            addedToRecordingThread = false;
        }
    }

    @Override
    public BooleanMeasure findRecord(TestExecutionContext testExecutionContext) {
        return null;
    }

    @Override
    public void cleanResources() {
        ConnectionListenerRegistry.unregister(connectionEventsProfiler);
    }

}
