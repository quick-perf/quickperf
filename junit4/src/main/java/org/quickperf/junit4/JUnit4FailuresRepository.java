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

package org.quickperf.junit4;

import org.junit.runner.notification.Failure;
import org.quickperf.RetrievableFailure;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;

import java.io.File;
import java.util.List;

public class JUnit4FailuresRepository implements RetrievableFailure {

    public static final JUnit4FailuresRepository INSTANCE = new JUnit4FailuresRepository();

    public static JUnit4FailuresRepository getInstance() {
        return INSTANCE;
    }

    private final String fileName = "junitFailures.ser";

    private final ObjectFileRepository objectFileRepository = ObjectFileRepository.INSTANCE;

    void save(String workingFolderPath, List<Failure> failures) {
        if(!failures.isEmpty()) {
            objectFileRepository.save(workingFolderPath, fileName, failures);
        }
    }

    public Throwable find(WorkingFolder workingFolder) {
        if(!junitFailuresFileExists(workingFolder.getPath())) {
            return null;
        }
        @SuppressWarnings("unchecked") List<Failure> failures
            = (List<Failure>) objectFileRepository.find(workingFolder.getPath(), fileName);
        return buildThrowable(failures);
    }

    private Throwable buildThrowable(List<Failure> failures) {
        if(failures.isEmpty()) {
            return null;
        }
        Failure firstFailure = failures.get(0);
        Throwable firstThrowable = firstFailure.getException();
        if(failures.size() > 1) {
            for (int i = 2; i < failures.size(); i++) {
                Failure failure = failures.get(i);
                Throwable throwable =  failure.getException();
                firstThrowable.addSuppressed(throwable);
            }
        }
        return firstThrowable;
    }

    private boolean junitFailuresFileExists(String workingFolderPath) {
        String filePath = workingFolderPath + File.separator + fileName;
        File file = new File(filePath);
        return file.exists();
    }

}
