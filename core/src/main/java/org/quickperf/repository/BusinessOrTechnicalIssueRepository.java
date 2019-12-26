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

import org.quickperf.issue.BusinessOrTechnicalIssue;
import org.quickperf.WorkingFolder;

import java.io.File;

public class BusinessOrTechnicalIssueRepository {

    public static final BusinessOrTechnicalIssueRepository INSTANCE = new BusinessOrTechnicalIssueRepository();

    private final String fileName = "businessOrTechnicalIssue.ser";

    private final ObjectFileRepository objectFileRepository = ObjectFileRepository.INSTANCE;

    private BusinessOrTechnicalIssueRepository() {}

    public void save(BusinessOrTechnicalIssue businessOrTechnicalIssue, String workingFolderPath) {
        if(businessOrTechnicalIssue.isNone()) {
           return;
        }
        objectFileRepository.save(workingFolderPath, fileName, businessOrTechnicalIssue);
    }

    public BusinessOrTechnicalIssue findFrom(WorkingFolder workingFolder) {
        if(serializationFileExists(workingFolder)) {
            return (BusinessOrTechnicalIssue) objectFileRepository.find(workingFolder.getPath(), fileName);
        }
        return BusinessOrTechnicalIssue.NONE;
    }

    private boolean serializationFileExists(WorkingFolder workingFolder) {
        String filePath = workingFolder.getPath() + File.separator + fileName;
        File file = new File(filePath);
        return file.exists();
    }

}
