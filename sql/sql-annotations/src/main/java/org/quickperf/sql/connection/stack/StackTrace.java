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
package org.quickperf.sql.connection.stack;

import java.util.Arrays;

public class StackTrace {

    public static class StackDepth {

        public static final StackDepth NONE = new StackDepth((short) 0);

        public static final StackDepth ALL = new StackDepth((short) 1_000);

        private final int maxValue;

        public StackDepth(short maxValue) {
            this.maxValue = maxValue;
        }

        private boolean isLessThan(int value) {
            return maxValue < value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StackDepth that = (StackDepth) o;
            return maxValue == that.maxValue;
        }

    }

    private final StackTraceElement[] allElements;

    private int firstPos;

    private int lastPos;

    static StackTrace of(StackTraceElement[] elements) {
        return new StackTrace(elements);
    }

    private StackTrace(StackTraceElement[] elements) {
        this.firstPos = 0;
        this.lastPos = elements.length - 1;
        this.allElements = elements;
    }

    StackTraceElement[] getElements() {
        return Arrays.copyOfRange(allElements, firstPos, lastPos + 1);
    }

    void formatWith(Iterable<StackTraceFilter> stackTraceFilters, StackDepth stackDepth) {
        if(StackTrace.StackDepth.NONE.equals(stackDepth)) {
           firstPos = 0;
           lastPos = 0;
           return;
        }
        for (StackTraceFilter stackTraceFilter : stackTraceFilters) {
            filter(stackTraceFilter);
        }
        limitTo(stackDepth);
    }

    private void filter(StackTraceFilter stackTraceFilter) {
        PositionsFiltering positionsFiltering = stackTraceFilter.filter(this.allElements);
        int startFilteringPosition = positionsFiltering.getStart();
        int endFilteringPosition = positionsFiltering.getEnd();
        if(startFilteringPosition > this.firstPos) {
            this.firstPos = startFilteringPosition;
        }
        if(endFilteringPosition < this.lastPos) {
            this.lastPos = endFilteringPosition;
        }
    }

    private void limitTo(StackDepth stackDepth) {
        int allStackLength = allElements.length;
        if (stackDepth.isLessThan(allStackLength)) {
           this.lastPos = this.firstPos + stackDepth.maxValue - 1;
        }
    }

}
