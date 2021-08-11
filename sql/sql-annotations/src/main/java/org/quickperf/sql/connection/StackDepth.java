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

package org.quickperf.sql.connection;

public class StackDepth {

    public static final StackDepth NONE = new StackDepth((short) 0);

    public static final StackDepth ALL = new StackDepth((short) 1_000);

    private final short maxValue;

    public StackDepth(short maxValue) {
        this.maxValue = maxValue;
    }

    public short getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StackDepth that = (StackDepth) o;
        return maxValue == that.maxValue;
    }

}
