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

package org.quickperf.junit5;

import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class JUnit5FailuresRepository {

    public static final JUnit5FailuresRepository INSTANCE = new JUnit5FailuresRepository();

    public static JUnit5FailuresRepository getInstance() {
        return INSTANCE;
    }

    private final String fileName = "junitFailures.ser";

    private final ObjectFileRepository objectFileRepository =  ObjectFileRepository.INSTANCE;

    void save(String workingFolderPath, List<TestExecutionSummary.Failure> failures) {
        if(!failures.isEmpty()) {
            List<DefaultFailure> mappedFailures = failures.stream().map(this::mapToDefaultFailure).collect(Collectors.toList());
            objectFileRepository.save(workingFolderPath, fileName, mappedFailures);
        }
    }

    public Throwable find(WorkingFolder workingFolder) {
        if(!junitFailuresFileExists(workingFolder.getPath())) {
            return null;
        }
        List<DefaultFailure> failures
                = (List<DefaultFailure>) objectFileRepository.find(workingFolder.getPath(), fileName);
        return buildThrowable(failures);
    }

    private Throwable buildThrowable(List<DefaultFailure> failures) {
        if(failures.isEmpty()) {
            return null;
        }
        TestExecutionSummary.Failure firstFailure = failures.get(0);
        Throwable firstThrowable = firstFailure.getException();
        if(failures.size() > 1) {
            for (int i = 2; i < failures.size(); i++) {
                TestExecutionSummary.Failure failure = failures.get(i);
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

    /**
     * We had to map to a custom DefaultFailure as the concrete type org.junit.platform.launcher.listeners.MutableTestExecutionSummary.Failure
     * is not seriaizable
     */
    private DefaultFailure mapToDefaultFailure(TestExecutionSummary.Failure failure){
        return new DefaultFailure(failure.getTestIdentifier(), failure.getException());
    }

    public static class DefaultFailure implements TestExecutionSummary.Failure, Serializable {
        private TestIdentifier testIdentifier;
        private Throwable exception;

        DefaultFailure(TestIdentifier testIdentifier, Throwable exception) {
            this.testIdentifier = testIdentifier;
            this.exception = exception;
        }

        @Override
        public TestIdentifier getTestIdentifier() {
            return testIdentifier;
        }

        public void setTestIdentifier(TestIdentifier testIdentifier) {
            this.testIdentifier = testIdentifier;
        }

        @Override
        public Throwable getException() {
            return exception;
        }

        public void setException(Throwable exception) {
            this.exception = exception;
        }
    }

}
