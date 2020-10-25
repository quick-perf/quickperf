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

package org.quickperf.junit5;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JUnit5ExtensionTest {

    @QuickPerfTest
    public static class JUnit5MethodFailing {

        @Test
        public void a_failing_test() {
            assertThat(false).isTrue();
        }

    }

    @Test public void
    a_test_with_failing_business_code_should_fail() {

        // GIVEN
        Class<JUnit5MethodFailing> testClass = JUnit5MethodFailing.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expecting:")
                               .contains("<false>")
                               .contains("to be equal to:")
                               .contains("<true>");

    }

    @QuickPerfTest
    public static class JUnit5MethodFailingInASpecificJvm {

        @HeapSize(value = 10, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void a_failing_test() {
            assertThat(false).isTrue();
        }

    }

    @Test public void
    a_test_with_failing_business_code_should_fail_when_the_test_method_is_executed_in_a_specific_jvm() {

        // GIVEN
        Class<?> testClass = JUnit5MethodFailingInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("Expecting:")
                               .contains("<false>")
                               .contains("to be equal to:")
                               .contains("<true>");

    }

    @QuickPerfTest
    public static class TestWithBeforeEachAndAfterEach {
        @BeforeEach
        void createLockFile() throws IOException {
            System.out.println("Creating the file");
            File file = new File("lock");
            if(!file.createNewFile()){
                throw new RuntimeException("Unable to create lock file");
            }
        }

        // this test will fork the VM so if we didn't skip the BeforeEach/AfterEach invocation
        // the file will be created twice
        @Test
        @MeasureHeapAllocation
        void checkLockFile() {
            File file = new File("lock");
            assertTrue(file.exists());
        }

        @AfterEach
        void deleteLockFile() {
            System.out.println("Deleting the file");
            File file = new File("lock");
            if(!file.delete()){
                throw new RuntimeException("Unable to delete lock file");
            }
        }
    }

    @Test public void
    a_test_with_before_each_and_after_each_must_work_on_forked_vm() {
        // GIVEN
        Class<?> testClass = TestWithBeforeEachAndAfterEach.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();

        String errorReport = jUnit5TestsResult.getErrorReport();
        System.err.println(errorReport);
        assertThat(errorReport).isNullOrEmpty();
    }
}
