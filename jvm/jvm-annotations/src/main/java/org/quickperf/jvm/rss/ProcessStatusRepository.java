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
package org.quickperf.jvm.rss;

import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;

public class ProcessStatusRepository {
    private static final String PROCESS_STATUS_FILE_NAME = "process-status.ser";

    public void save(ProcessStatus status, TestExecutionContext testExecutionContext) {
        ObjectFileRepository repository = ObjectFileRepository.INSTANCE;
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        repository.save(workingFolder.getPath(), PROCESS_STATUS_FILE_NAME, status);
    }

    public ProcessStatus find(TestExecutionContext testExecutionContext) {
        ObjectFileRepository repository = ObjectFileRepository.INSTANCE;
        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        return (ProcessStatus) repository.find(workingFolder.getPath() , PROCESS_STATUS_FILE_NAME);
    }
}
