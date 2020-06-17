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
