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
package org.quickperf.writer;

import java.io.PrintWriter;
import java.io.Writer;

public class DefaultWriterFactory implements WriterFactory {

    @Override
    public Writer buildWriter() {
        return SystemOutPrintWriterInstance.INSTANCE.getSystemOutPrintWriter();
    }

    public enum SystemOutPrintWriterInstance {

        INSTANCE;

        private final SystemOutPrintWriter systemOutPrintWriter;

        SystemOutPrintWriterInstance() {
            this.systemOutPrintWriter = new SystemOutPrintWriter();
        }

        public SystemOutPrintWriter getSystemOutPrintWriter() {
            return systemOutPrintWriter;
        }
    }

    private static class SystemOutPrintWriter extends PrintWriter {

        private SystemOutPrintWriter() {
            super(System.out);
        }

        @Override
        public void close() {
            // this class is built on System.out and
            // should not close it
            super.flush();
        }
    }
}
