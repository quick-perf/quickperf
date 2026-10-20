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
 * arguments, in the same order, in every JVM. Likewise, the unique id of a dynamic test contains its index and the
 * indexes of its dynamic containers, for example <code>[dynamic-container:#1]/[dynamic-test:#2]</code>: the new JVM
 * executes the test factory method again, and then only this dynamic test. So the test factory method has to provide
 * the same dynamic tests and dynamic containers, in the same order, in every JVM. The other dynamic tests of the test
 * factory are not executed in the new JVM: a dynamic test cannot depend on the dynamic tests executed before it.
 * <p>
 * The test JVM also saves the display name of the test: the new JVM fails the test when the dynamic test having the
 * unique id has another display name there.
 */
class TestToExecuteInNewJvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "junit5-test-to-execute.ser";

    private final String uniqueId;

    private final String displayName;

    private TestToExecuteInNewJvm(String uniqueId, String displayName) {
        this.uniqueId = uniqueId;
        this.displayName = displayName;
    }

    static TestToExecuteInNewJvm buildFrom(ExtensionContext extensionContext) {
        return new TestToExecuteInNewJvm(extensionContext.getUniqueId(), extensionContext.getDisplayName());
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

    String getDisplayName() {
        return displayName;
    }

}
