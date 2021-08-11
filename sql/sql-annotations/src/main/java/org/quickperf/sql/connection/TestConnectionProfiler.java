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

package org.quickperf.sql.connection;

import org.quickperf.TestExecutionContext;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.perfrecording.RecordablePerformance;

public class TestConnectionProfiler implements RecordablePerformance<BooleanMeasure> {

    private final ConnectionEventsProfiler connectionEventsProfiler;

    public TestConnectionProfiler(AnnotationProfilingParameters annotationProfilingParameters) {
        ProfilingParameters profilingParameters = annotationProfilingParameters.getProfilingParameters();
        connectionEventsProfiler = new ConnectionEventsProfiler(profilingParameters);
        if(annotationProfilingParameters.isBeforeAndAfterTestMethodExecution()) {
            connectionEventsProfiler.start();
        }
        ConnectionListenerRegistry.INSTANCE.register(connectionEventsProfiler);
    }

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        connectionEventsProfiler.start();
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        connectionEventsProfiler.stop();
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
