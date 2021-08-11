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

public class StackTraceDisplayConfig {

    public static final StackTraceDisplayConfig NONE = new StackTraceDisplayConfig(false, StackDepth.NONE);

    private final StackDepth stackDepth;

    private final boolean stackTraceFiltered;

    public StackTraceDisplayConfig(boolean stackTraceFiltered
                                 , StackDepth stackDepth) {
        this.stackTraceFiltered = stackTraceFiltered;
        this.stackDepth = stackDepth;
    }

    public boolean isStackTraceFiltered() {
        return stackTraceFiltered;
    }

    public StackDepth getStackDepth() {
        return stackDepth;
    }

}
