/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.issue;

public class JvmError extends Error {

    public static JvmError buildFrom(String message) {
        JvmError jvmError = new JvmError(message);
        jvmError.resetStackTrace();
        return jvmError;
    }

    public static JvmError buildFrom(Exception e) {
        JvmError jvmError = new JvmError(e);
        jvmError.resetStackTrace();
        return jvmError;
    }

    private JvmError(String message) {
        super(message);
    }

    private JvmError(Exception e) {
        super(e);
    }

    private void resetStackTrace() {
        this.setStackTrace(new StackTraceElement[0]);
    }

}
