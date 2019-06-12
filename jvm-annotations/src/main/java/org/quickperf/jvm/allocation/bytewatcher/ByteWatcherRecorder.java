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

package org.quickperf.jvm.allocation.bytewatcher;

import org.quickperf.TestExecutionContext;
import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationRepository;
import org.quickperf.perfrecording.RecordablePerformance;

public class ByteWatcherRecorder implements RecordablePerformance<Allocation> {

    private AllocationRepository allocationRepository;

    private ByteWatcherSingleThread byteWatcher;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        allocationRepository = new AllocationRepository();
        byteWatcher = new ByteWatcherSingleThread(Thread.currentThread());
        byteWatcher.reset();
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        int junit4ByteOffset = retrieveJunit4ByteOffset();
        long allocationInBytes = byteWatcher.calculateAllocations() - junit4ByteOffset;
        allocationRepository.saveAllocationInBytes(allocationInBytes, testExecutionContext);
    }

    private int retrieveJunit4ByteOffset() {

        String jvmVersionAsString = System.getProperty("java.vm.specification.version");

        if("11".equals(jvmVersionAsString)) {
            return 160;
        }

        if("12".equals(jvmVersionAsString)) {
            return 240;
        }

        if("1.7".contains(jvmVersionAsString)) {
            return 136;
        }

        return 40;

    }

    @Override
    public Allocation findRecord(TestExecutionContext testExecutionContext) {
        allocationRepository = new AllocationRepository();
        return allocationRepository.findAllocation(testExecutionContext);
    }

    @Override
    public void cleanResources() {
    }

}
