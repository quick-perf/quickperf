/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.issue;

public class TestException extends Exception {

    private TestException(String message) {
        super(message);
    }

    public static TestException buildFrom(String message) {
        TestException testException = new TestException(message);
        resetStackTraceOf(testException);
        return testException;
    }

    private static void resetStackTraceOf(Throwable cause) {
        cause.setStackTrace(new StackTraceElement[0]);
    }

}
