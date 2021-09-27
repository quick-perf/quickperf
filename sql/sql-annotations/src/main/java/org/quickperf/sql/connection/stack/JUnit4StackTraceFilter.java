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

public class JUnit4StackTraceFilter implements StackTraceFilter {

    public static final JUnit4StackTraceFilter INSTANCE = new JUnit4StackTraceFilter();

    @Override
    public PositionsFiltering filter(StackTraceElement[] stackElements) {
        int zeroPos = 0;
        int quickPerMethodPos = extractQuickPerfMethodPos(stackElements);
        int junit4LastPos = extractJUnit4LastPost(stackElements, quickPerMethodPos);
        return new PositionsFiltering(zeroPos, junit4LastPos);
    }

    private int extractQuickPerfMethodPos(StackTraceElement[] stackElements) {
        for (int pos = 0; pos < stackElements.length; pos++) {
            String stackTraceElementAsString = stackElements[pos].toString();
            if (stackTraceElementAsString.contains("org.quickperf.junit4.QuickPerfMethod")) {
                return pos;
            }
        }
        return 0;
    }

    private int extractJUnit4LastPost(StackTraceElement[] stackElements, int quickPerMethodPos) {
        for (int pos = (quickPerMethodPos - 1); pos > 0; pos--) {
            String stackTraceElementAsString = stackElements[pos].toString();
            if (!stackTraceElementAsString.startsWith("java")
             && !stackTraceElementAsString.startsWith("sun.reflect")) {
                return pos;
            }
        }
        return stackElements.length;
    }

}
