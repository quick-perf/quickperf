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

class StackTracerPrinter {

    static final StackTracerPrinter NONE = new StackTracerPrinter() {
        @Override
        void printStackTrace() {
        }
    };

    private StackTraceDisplayConfig stacktracedisplayConfig;

    private PrintWriter printWriter;

    static StackTracerPrinter buildFrom(StackTraceDisplayConfig stacktracedisplayConfig, PrintWriter printWriter) {
        if(stacktracedisplayConfig.equals(StackTraceDisplayConfig.NONE)) {
            return NONE;
        }
        return new StackTracerPrinter(stacktracedisplayConfig, printWriter);
    }

    private StackTracerPrinter() {}

    private StackTracerPrinter(StackTraceDisplayConfig stacktracedisplayConfig, PrintWriter printWriter) {
        this.stacktracedisplayConfig = stacktracedisplayConfig;
        this.printWriter = printWriter;
    }

    void printStackTrace() {
        StackTraceElement[] currentStackTrace
                = Thread.currentThread().getStackTrace();
        StackTraceElement[] elementsToDisplay
                = buildStackElementsToDisplayFrom( currentStackTrace
                                                 , stacktracedisplayConfig);
        print(elementsToDisplay, printWriter);
    }

    private StackTraceElement[] buildStackElementsToDisplayFrom(StackTraceElement[] stackTraceElements, StackTraceDisplayConfig stacktracedisplayConfig) {
        StackTrace stackTrace = StackTrace.of(stackTraceElements);

        if(stacktracedisplayConfig.isStackTraceFiltered()) {
            stackTrace = StackTrace.of(stackTraceElements)
                        .filterFrameworks();
        }

        StackDepth stackDepth = stacktracedisplayConfig.getStackDepth();
        return stackTrace.limitDepthTo(stackDepth)
                         .getElements();
    }

    private void print(StackTraceElement[] elementsToDisplay, PrintWriter printWriter) {
        for (StackTraceElement stackTraceElement : elementsToDisplay) {
            printWriter.println("\t" + stackTraceElement);
            printWriter.flush();
        }
    }

}
