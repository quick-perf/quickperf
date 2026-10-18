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

import org.junit.platform.launcher.TestIdentifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test method executed with {@link JUnit5Tests} can fail with {@link #reportExecutionOf(String)} to report in which
 * JVM it was executed, and what it executed. The test executing it gets the reports with
 * {@link JUnit5Tests.JUnit5TestsResult#getJvmExecutionReports()}.
 * <p>
 * QuickPerf reports the first failure of a test executed in a specific JVM: a report does not tell whether this JVM
 * executed other tests after it.
 */
public class JvmExecutionReport {

    // Unlike a process id, which the operating system can give to a JVM started after the end of another one
    private static final String THIS_JVM_ID = UUID.randomUUID().toString();

    private static final Pattern MESSAGE_PATTERN = Pattern.compile("Executed in the JVM (\\S+): (.*)", Pattern.DOTALL);

    private final String testUniqueId;

    private final String testDisplayName;

    private final String jvmId;

    private final String executedTest;

    private JvmExecutionReport(String testUniqueId, String testDisplayName, String jvmId, String executedTest) {
        this.testUniqueId = testUniqueId;
        this.testDisplayName = testDisplayName;
        this.jvmId = jvmId;
        this.executedTest = executedTest;
    }

    /**
     * @param executedTest what the test method executed, for example its arguments
     */
    public static AssertionError reportExecutionOf(String executedTest) {
        return new AssertionError("Executed in the JVM " + THIS_JVM_ID + ": " + executedTest);
    }

    static JvmExecutionReport buildFrom(TestIdentifier testIdentifier, Throwable testFailure) {
        String message = testFailure.getMessage();
        Matcher matcher = MESSAGE_PATTERN.matcher(message == null ? "" : message);
        if (!matcher.matches()) {
            return null;
        }
        return new JvmExecutionReport(testIdentifier.getUniqueId()
                                    , testIdentifier.getDisplayName()
                                    , matcher.group(1)
                                    , matcher.group(2));
    }

    public static void assertThatEachTestWasExecutedInItsOwnNewJvm(List<JvmExecutionReport> reports) {
        List<String> jvmIds = new ArrayList<>();
        for (JvmExecutionReport report : reports) {
            jvmIds.add(report.jvmId);
        }
        assertThat(jvmIds).as("JVMs executing the tests")
                          .doesNotHaveDuplicates()
                          .doesNotContain(THIS_JVM_ID);
    }

    // The test JVM reports the execution of a new JVM for the test that this new JVM executed
    public static void assertThatEachTestWasTheTestExecutedInItsNewJvm(List<JvmExecutionReport> reports) {
        for (JvmExecutionReport report : reports) {
            assertThat(report.executedTest)
                    .as("Test executed in the new JVM of %s", report.testUniqueId)
                    .isEqualTo(report.testDisplayName);
        }
    }

    /**
     * The JUnit unique id, in the test JVM, of the test that failed with this report
     */
    public String getTestUniqueId() {
        return testUniqueId;
    }

    /**
     * The JUnit display name, in the test JVM, of the test that failed with this report
     */
    public String getTestDisplayName() {
        return testDisplayName;
    }

    public String getExecutedTest() {
        return executedTest;
    }

    @Override
    public String toString() {
        return testUniqueId + " -> executed in the JVM " + jvmId + ": " + executedTest;
    }

}
