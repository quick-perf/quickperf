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
package org.quickperf.perfrecording;

/**
 * A {@link RecordablePerformance} recording what the thread building the test
 * execution context executes, from the code executed before the test method
 * to the code executed after it.
 * <p>
 * A test framework executing this code on another thread, such as JUnit 4
 * under a {@code Timeout} rule, calls {@link #bindToCurrentThread()} on that
 * thread before executing it, and {@link RecordablePerformance#cleanResources()}
 * on the same thread after it, which unbinds the recorder from the thread.
 */
public interface ThreadBoundPerfRecorder {

    /**
     * Also records what the current thread executes. Does nothing if the
     * recorder already records it.
     */
    void bindToCurrentThread();

}
