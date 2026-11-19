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

import org.junit.runners.model.Statement;
import org.quickperf.perfrecording.TestThreads;

/**
 * Marks the thread evaluating it as a test thread, then evaluates the wrapped
 * statement. JUnit may evaluate a statement of a test on another thread than
 * the one running the test: a thread started by {@code @Test(timeout = ...)}
 * or by a {@code Timeout} rule, for example.
 *
 * @see TestThreads
 */
public class TestThreadMarkingStatement extends Statement {

    private final Statement next;

    public TestThreadMarkingStatement(Statement next) {
        this.next = next;
    }

    @Override
    public void evaluate() throws Throwable {
        TestThreads.markCurrentThread();
        next.evaluate();
    }

}
