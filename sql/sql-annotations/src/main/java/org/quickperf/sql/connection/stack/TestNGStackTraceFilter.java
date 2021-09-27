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

package org.quickperf.sql.connection.stack;

public class TestNGStackTraceFilter implements StackTraceFilter {

    public static final TestNGStackTraceFilter INSTANCE = new TestNGStackTraceFilter();

    @Override
    public PositionsFiltering filter(StackTraceElement[] stackElements) {
        int zeroPos = 0;
        int quickPerfTestNGListenerPos = extractQuickPerfTestNGListenerPos(stackElements);
        int testNGLastPos = extractTestNGLastPos(stackElements, quickPerfTestNGListenerPos);
        return new PositionsFiltering(zeroPos, testNGLastPos);
    }

    private int extractQuickPerfTestNGListenerPos(StackTraceElement[] stackElements) {
        for (int pos = 0; pos < stackElements.length; pos++) {
            String stackTraceElementAsString = stackElements[pos].toString();
            if (stackTraceElementAsString.contains("org.quickperf.testng.QuickPerfTestNGListener")) {
                return pos;
            }
        }
        return (short) 0;
    }

    private int extractTestNGLastPos(StackTraceElement[] stackElements, int quickPerfTestNGListenerPos) {
        for (int pos = (quickPerfTestNGListenerPos - 1); pos > 0; pos--) {
            String stackTraceElementAsString = stackElements[pos].toString();
            if (!stackTraceElementAsString.startsWith("java")
             && !stackTraceElementAsString.startsWith("org.testng")
             && !stackTraceElementAsString.startsWith("sun.reflect")) {
                return pos;
            }
        }
        return stackElements.length;
    }

}
