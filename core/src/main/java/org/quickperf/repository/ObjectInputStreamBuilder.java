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

package org.quickperf.repository;

import java.io.*;

class ObjectInputStreamBuilder {

    static final ObjectInputStreamBuilder INSTANCE = new ObjectInputStreamBuilder();

    static ObjectInputStreamBuilder getInstance() {
        return INSTANCE;
    }

    public ObjectInputStream buildObjectInputStream(String workingFolderPath, String fileName) {

        File tempFile = buildTempFile(workingFolderPath, fileName);

        FileInputStream fileInputStream = buildFileInputStream(tempFile);

        try {
            return new ObjectInputStream(fileInputStream);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private File buildTempFile(String workingFolderPath, String fileName) {
        String filePath = workingFolderPath + File.separator + fileName;
        return new File(filePath);
    }

    private FileInputStream buildFileInputStream(File tempFile) {
        try {
            return new FileInputStream(tempFile);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

}
