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

package org.quickperf.jvm.allocation;

import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.LongFileRepository;
import org.quickperf.repository.LongRepository;

public class AllocationRepository {

    private static final String BYTE_WATCHER_FILE_NAME = "allocation.ser";

    public void saveAllocationInBytes(long allocation, TestExecutionContext testExecutionContext) {
        LongRepository longRepository = new LongFileRepository();
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        longRepository.save(allocation, workingFolder.getPath(), BYTE_WATCHER_FILE_NAME);
    }

    public Allocation findAllocation(TestExecutionContext testExecutionContext) {
        LongRepository longRepository = new LongFileRepository();
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        long allocationInBytes = longRepository.find(workingFolder.getPath(), BYTE_WATCHER_FILE_NAME);
        return Allocation.ofBytes(allocationInBytes);
    }

}
