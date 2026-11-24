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

/**
 * In the new JVM, wraps the <code>@Before</code> methods, the test method and the <code>@After</code> methods. A rule
 * retrying the test evaluates this statement again: an abort of a <code>@Before</code> method must not be taken for an
 * abort thrown after the test method of a previous attempt.
 *
 * @see TestMethodCompletionInNewJvm
 */
class TestMethodCompletionResettingStatement extends Statement {

    private final Statement junitAfters;

    TestMethodCompletionResettingStatement(Statement junitAfters) {
        this.junitAfters = junitAfters;
    }

    @Override
    public void evaluate() throws Throwable {
        TestMethodCompletionInNewJvm.INSTANCE.record(false);
        junitAfters.evaluate();
    }

}
