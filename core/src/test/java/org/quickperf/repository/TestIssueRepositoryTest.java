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

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.quickperf.WorkingFolder;
import org.quickperf.issue.TestIssue;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIssueRepositoryTest {

    // As the specific JVM, which saves the test issue for the test JVM
    @Test public void
    should_keep_an_abort_thrown_after_the_test_method_marked_as_such() {

        // GIVEN
        WorkingFolder workingFolder = WorkingFolder.createOrRetrieveWorkingFolder(true);
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");
        TestIssue testIssue = TestIssue.buildFromThrowableAfterTestMethod(assumptionFailure);

        // WHEN
        TestIssueRepository.INSTANCE.save(testIssue, workingFolder.getPath());
        TestIssue foundTestIssue = TestIssueRepository.INSTANCE.findFrom(workingFolder);

        // THEN
        assertThat(foundTestIssue.asThrowable()).isInstanceOf(AssumptionViolatedException.class)
                                                .hasMessage("assumption not respected");
        assertThat(foundTestIssue.isAbort()).isTrue();
        assertThat(foundTestIssue.isThrownAfterTestMethod()).isTrue();

    }

    @Test public void
    should_keep_an_abort_that_cannot_be_serialized_as_an_abort() {

        // GIVEN
        WorkingFolder workingFolder = WorkingFolder.createOrRetrieveWorkingFolder(true);
        TestIssue testIssue = TestIssue.buildFrom(new NotSerializableAssumptionViolatedException());

        // WHEN
        TestIssueRepository.INSTANCE.save(testIssue, workingFolder.getPath());
        TestIssue foundTestIssue = TestIssueRepository.INSTANCE.findFrom(workingFolder);

        // THEN
        assertThat(foundTestIssue.asThrowable()).hasMessageContaining("assumption not respected");
        assertThat(foundTestIssue.isAbort()).isTrue();
        assertThat(foundTestIssue.isThrownAfterTestMethod()).isFalse();

    }

    private static class NotSerializableAssumptionViolatedException extends AssumptionViolatedException {

        private final Object notSerializable = new Object();

        NotSerializableAssumptionViolatedException() {
            super("assumption not respected");
        }

    }

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