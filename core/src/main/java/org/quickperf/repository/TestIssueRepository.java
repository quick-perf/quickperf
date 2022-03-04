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
package org.quickperf.repository;

import org.quickperf.WorkingFolder;
import org.quickperf.issue.TestIssue;

import java.io.File;
import java.io.NotSerializableException;

public class TestIssueRepository {

    public static final TestIssueRepository INSTANCE = new TestIssueRepository();

    private final String fileName = "testIssue.ser";

    private final ObjectFileRepository objectFileRepository = ObjectFileRepository.INSTANCE;

    private TestIssueRepository() {}

    public void save(TestIssue testIssue, String workingFolderPath) {

        if(testIssue.isNone()) {
           return;
        }

        try {
            objectFileRepository.save(workingFolderPath, fileName, testIssue);
        } catch (IllegalStateException illegalStateException) {
            manageSavingIssue(testIssue, workingFolderPath, illegalStateException);
        }

    }

    private void manageSavingIssue(TestIssue testIssue, String workingFolderPath, IllegalStateException illegalStateException) {
        Throwable cause = illegalStateException.getCause();
        if (cause instanceof NotSerializableException) {
            TestIssue serializableTestIssue =
                    TestIssue.buildSerializableTestIssueFrom(testIssue);
            save(serializableTestIssue, workingFolderPath);
        } else {
            throw illegalStateException;
        }
    }

    public TestIssue findFrom(WorkingFolder workingFolder) {
        if(serializationFileExists(workingFolder)) {
            return (TestIssue) objectFileRepository.find(workingFolder.getPath(), fileName);
        }
        return TestIssue.NONE;
    }

    private boolean serializationFileExists(WorkingFolder workingFolder) {
        String filePath = workingFolder.getPath() + File.separator + fileName;
        File file = new File(filePath);
        return file.exists();
    }

}
