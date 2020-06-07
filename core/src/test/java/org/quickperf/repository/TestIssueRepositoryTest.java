/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.repository;

import org.junit.Test;
import org.quickperf.issue.TestIssue;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TestIssueRepositoryTest {

    @Test public void
    should_not_fail_if_throwable_is_not_serializable() {

        Path targetDirectory = Paths.get("target");
        String workingFolderPath = targetDirectory.toFile().getAbsolutePath();

        Throwable notSerializableThrowable = new NotSerializableThrowable();

        TestIssue testIssueWithNonSerializableThrowable = TestIssue.buildFrom(notSerializableThrowable);

        TestIssueRepository.INSTANCE.save(testIssueWithNonSerializableThrowable
                                        , workingFolderPath);

    }

}