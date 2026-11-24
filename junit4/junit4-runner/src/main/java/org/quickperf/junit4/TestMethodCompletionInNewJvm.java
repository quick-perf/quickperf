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
package org.quickperf.junit4;

/**
 * The new JVM executes one test. There, {@link QuickPerfMethod} records whether the test method completed: it returned,
 * or threw the exception expected by <code>@Test(expected = ...)</code>, other than a failing assumption.
 * {@link JUnit4TestRunner} reads it once JUnit has executed the test.
 */
class TestMethodCompletionInNewJvm {

    static final TestMethodCompletionInNewJvm INSTANCE = new TestMethodCompletionInNewJvm();

    // Written by the thread executing the test method, read by the thread that started JUnit
    private volatile boolean testMethodCompleted;

    private TestMethodCompletionInNewJvm() { }

    void record(boolean testMethodCompleted) {
        this.testMethodCompleted = testMethodCompleted;
    }

    boolean isTestMethodCompleted() {
        return testMethodCompleted;
    }

}
