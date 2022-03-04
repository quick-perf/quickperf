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
package org.quickperf.issue;

public class JvmOrTestIssue {

    public static final JvmOrTestIssue NONE = new JvmOrTestIssue();

    private TestIssue testIssue = TestIssue.NONE;

    private JvmIssue jvmIssue = JvmIssue.NONE;

    private JvmOrTestIssue() { }

    public JvmIssue getJvmIssue() {
        return jvmIssue;
    }

    public TestIssue getTestIssue() {
        return testIssue;
    }

    public boolean hasJvmIssue() {
        return jvmIssue != JvmIssue.NONE;
    }

    public static JvmOrTestIssue buildFrom(TestIssue testIssue) {
        JvmOrTestIssue jvmOrTestIssue = new JvmOrTestIssue();
        jvmOrTestIssue.testIssue = testIssue;
        return jvmOrTestIssue;
    }

    public static JvmOrTestIssue buildFrom(JvmIssue jvmIssue) {
        JvmOrTestIssue jvmOrTestIssue = new JvmOrTestIssue();
        jvmOrTestIssue.jvmIssue = jvmIssue;
        return jvmOrTestIssue;
    }

}
