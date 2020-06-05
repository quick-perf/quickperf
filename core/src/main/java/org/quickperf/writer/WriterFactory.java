package org.quickperf.writer;

import java.io.IOException;
import java.io.Writer;

/**
 * The interface that models the building of a <code>Writer</code> used to write messages generated
 * by the Heap Allocation Perf Verifier of QuickPerf.
 */
public interface WriterFactory {

    /**
     * This method is called by QuickPerf to build the <code>Writer</code>.
     * </p>
     * In case an exception is raised in the process, QuickPerf will use the default <code>Writer</code>
     * built on <code>System.out</code>.
     *
     * @return an instance of the <code>Writer</code> used by QuickPerf to write messages
     * @throws IOException
     */
    Writer buildWriter() throws IOException;
}