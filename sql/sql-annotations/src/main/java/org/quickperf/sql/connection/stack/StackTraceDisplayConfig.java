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

import static java.util.Collections.emptyList;

public class StackTraceDisplayConfig {

    private StackTrace.StackDepth stackDepth;

    private Iterable<StackTraceFilter> stackTraceFilters;

    private StackTraceDisplayConfig() {}

    public static StackTraceDisplayConfig of( StackTrace.StackDepth stackDepth
                                            , Iterable<StackTraceFilter> stackTraceFilters) {
        StackTraceDisplayConfig stackTraceDisplayConfig = new StackTraceDisplayConfig();
        stackTraceDisplayConfig.stackDepth = stackDepth;
        stackTraceDisplayConfig.stackTraceFilters = stackTraceFilters;
        return stackTraceDisplayConfig;
    }

    public static StackTraceDisplayConfig noStackTrace() {
        StackTraceDisplayConfig stackTraceDisplayConfig = new StackTraceDisplayConfig();
        stackTraceDisplayConfig.stackDepth = StackTrace.StackDepth.NONE;
        stackTraceDisplayConfig.stackTraceFilters = emptyList();
        return stackTraceDisplayConfig;
    }

    public StackTraceElement[] format(StackTraceElement[] stackTraceElements) {
        StackTrace stackTrace = StackTrace.of(stackTraceElements);
        stackTrace.formatWith(this.stackTraceFilters, this.stackDepth);
        return stackTrace.getElements();
    }

    public boolean isStackTraceDisplayed() {
        return !StackTrace.StackDepth.NONE.equals(stackDepth);
    }

}
