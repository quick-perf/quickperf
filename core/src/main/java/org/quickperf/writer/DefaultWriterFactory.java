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
