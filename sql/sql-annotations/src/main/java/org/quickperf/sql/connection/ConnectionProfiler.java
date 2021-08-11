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
import java.sql.Connection;

public class ConnectionProfiler {

    private final PrintWriter printWriter;

    private final StackTracerPrinter stackTracerPrinter;

    private boolean enabled;

    ConnectionProfiler( StackTraceDisplayConfig stacktracedisplayConfig
                      , PrintWriter printWriter) {
        this.printWriter = printWriter;
        this.stackTracerPrinter =
                StackTracerPrinter.buildFrom( stacktracedisplayConfig
                                            , printWriter);
    }

    void enables() {
        enabled = true;
    }

    void disables() {
        enabled = false;
    }

    void profile(Connection connection, String eventDescription) {
        if (enabled) {
            String connectionDescription = buildConnectionDescription(connection);
            String eventText = connectionDescription + " - " + eventDescription;
            printWriter.println(eventText);
            printWriter.flush();
            stackTracerPrinter.printStackTrace();
        }
    }

    private String buildConnectionDescription(Connection connection) {
        return "connection " + computeIdentifier(connection);
    }

    private int computeIdentifier(Connection connection) {
        return connection.hashCode();
    }

}
