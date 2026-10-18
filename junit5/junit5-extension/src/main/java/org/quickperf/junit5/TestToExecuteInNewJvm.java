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
package org.quickperf.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;

import java.io.File;
import java.io.Serializable;

/**
 * The test JVM saves the JUnit unique id of the test in the working folder, and the new JVM executes the test having
 * this unique id. The unique id of a test template invocation contains its index, for example
 * <code>[test-template-invocation:#2]</code>: so the arguments source of a parameterized test has to provide the same
 * arguments, in the same order, in every JVM.
 */
class TestToExecuteInNewJvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "junit5-test-to-execute.ser";

    private final String uniqueId;

    private TestToExecuteInNewJvm(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    static TestToExecuteInNewJvm buildFrom(ExtensionContext extensionContext) {
        return new TestToExecuteInNewJvm(extensionContext.getUniqueId());
    }

    void saveIn(WorkingFolder workingFolder) {
        ObjectFileRepository.INSTANCE.save(workingFolder, FILE_NAME, this);
    }

    static TestToExecuteInNewJvm findIn(String workingFolderPath) {
        File file = new File(workingFolderPath, FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        return (TestToExecuteInNewJvm) ObjectFileRepository.INSTANCE.find(workingFolderPath, FILE_NAME);
    }

    String getUniqueId() {
        return uniqueId;
    }

}
