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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

public class StringRepository {

    public static StringRepository INSTANCE = new StringRepository();

    public static StringRepository getInstance() {
        return INSTANCE;
    }

    public void save(List<String> lines, String workingFolderPath, String fileName) {
        File file = new File(workingFolderPath + File.separator + fileName);
        try {
            FileUtils.writeLines(file, lines);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Collection<String> findLines(String workingFolderPath, String fileName) {
        File file = new File(workingFolderPath + File.separator + fileName);
        try {
            return FileUtils.readLines(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String find(String workingFolderPath, String fileName) {
        File file = new File(workingFolderPath + File.separator + fileName);
        try {
            return FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
