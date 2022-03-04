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
package org.quickperf;

import java.io.IOException;
import java.nio.file.Files;

public class WorkingFolder {

    private static final WorkingFolder NONE = new WorkingFolder("");

    private final String path;

    private WorkingFolder(String path) {
        this.path = path;
    }

    public static WorkingFolder createOrRetrieveWorkingFolder(boolean hasTestMethodToBeLaunchedInASpecificJvm) {

        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            String path = SystemProperties.WORKING_FOLDER.evaluate();
            return new WorkingFolder(path);
        }

        if (hasTestMethodToBeLaunchedInASpecificJvm) {
            String path = createTempDirectory();
            return new WorkingFolder(path);
        }

        return NONE;

    }

    private static String createTempDirectory() {
        try {
            return Files.createTempDirectory("QuickPerf-").toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getPath() {
        return path;
    }

}
