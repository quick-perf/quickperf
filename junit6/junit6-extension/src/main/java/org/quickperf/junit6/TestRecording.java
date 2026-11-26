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
package org.quickperf.junit6;

import org.quickperf.TestExecutionContext;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The recording of a test: the extension instance recording it, the context it built, and whether this instance
 * executed the test method. Kept in the store of the extension context of the test method, test template invocation or
 * dynamic test, never in a class or test factory context: Store.get also searches the parent contexts.
 * <p>
 * JUnit registers the instance of every <code>@RegisterExtension</code> field, even when <code>@QuickPerfTest</code> or
 * the autodetection already registered the extension: two instances can then intercept a test. The first instance
 * building a context records the test, and the other instances only mark the threads and proceed.
 */
record TestRecording(QuickPerfTestExtension recordingExtension
                   , TestExecutionContext testExecutionContext
                   , AtomicBoolean testMethodExecuted) {

    TestRecording(QuickPerfTestExtension recordingExtension, TestExecutionContext testExecutionContext) {
        this(recordingExtension, testExecutionContext, new AtomicBoolean());
    }

    boolean isRecordedBy(QuickPerfTestExtension extension) {
        return recordingExtension == extension;
    }

    void markTestMethodExecuted() {
        testMethodExecuted.set(true);
    }

    boolean isTestMethodExecuted() {
        return testMethodExecuted.get();
    }

}
