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

import org.quickperf.JUnitVersion;
import org.quickperf.TestExecutionContext;
import org.quickperf.jvm.JvmVersion;
import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationRepository;
import org.quickperf.perfrecording.RecordablePerformance;

public class ByteWatcherRecorder implements RecordablePerformance<Allocation> {

    private static final boolean IS_JVM_VERSION_AT_LEAST_12 = JvmVersion.isGreaterThanOrEqualTo12();

    private AllocationRepository allocationRepository;

    private ByteWatcherSingleThread byteWatcher;

    private int junit4ByteOffset = 40;
    private int junit5ByteOffset = 56;
    private int java12ByteOffsetForJunit4 = 32;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        allocationRepository = new AllocationRepository();
        byteWatcher = new ByteWatcherSingleThread(Thread.currentThread());
        byteWatcher.reset();
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        int junitByteOffset = retrieveJunitByteOffset(testExecutionContext);
        long allocationInBytes = byteWatcher.calculateAllocations() - junitByteOffset;
        allocationRepository.saveAllocationInBytes(allocationInBytes, testExecutionContext);
    }

    private int retrieveJunitByteOffset(TestExecutionContext testExecutionContext) {
        int byteOffset = testExecutionContext.getjUnitVersion() == JUnitVersion.JUNIT4 ? junit4ByteOffset : junit5ByteOffset;
        if (testExecutionContext.getjUnitVersion() == JUnitVersion.JUNIT4 && IS_JVM_VERSION_AT_LEAST_12) {
            byteOffset += java12ByteOffsetForJunit4;
        }
        return byteOffset;
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
