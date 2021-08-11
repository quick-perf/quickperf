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

import java.io.PrintWriter;

public class ProfilingParameters {

    private final Level level;

    private final PrintWriter printWriter;

    private final StackTraceDisplayConfig stacktracedisplayConfig;

    public ProfilingParameters( Level level
                              , StackTraceDisplayConfig stacktracedisplayConfig
                              , PrintWriter printWriter) {
        this.level = level;
        this.stacktracedisplayConfig = stacktracedisplayConfig;
        this.printWriter = printWriter;
    }

    public Level getLevel() {
        return level;
    }

    public StackTraceDisplayConfig getStackTraceDisplayConfig() {
        return stacktracedisplayConfig;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

}