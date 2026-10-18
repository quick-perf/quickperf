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

import org.opentest4j.TestAbortedException;

/**
 * Sent by the new JVM when JUnit aborted the test before QuickPerf invoked the test method, for example because an
 * assumption failed in a <code>@BeforeEach</code> or <code>@BeforeAll</code> method. Its cause is the abort. The test
 * JVM reports this abort without evaluating performance records, because no performance was recorded.
 */
class TestAbortedBeforeTestMethodInNewJvm extends TestAbortedException {

    private static final long serialVersionUID = 1L;

    TestAbortedBeforeTestMethodInNewJvm(Throwable abort) {
        super(abort.getMessage(), abort);
    }

}
