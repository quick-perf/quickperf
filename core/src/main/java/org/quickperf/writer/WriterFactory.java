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

package org.quickperf.writer;

import java.io.IOException;
import java.io.Writer;

/**
 * The interface that models the building of a <code>Writer</code> used to write messages generated
 * by the Heap Allocation Perf Verifier of QuickPerf.
 */
public interface WriterFactory {

    /**
     * <p>
     * This method is called by QuickPerf to build the <code>Writer</code>.
     * </p>
     * <p>In case an exception is raised in the process, QuickPerf will use the default <code>Writer</code>
     * built on <code>System.out</code>.<p>
     *
     * @return an instance of the <code>Writer</code> used by QuickPerf to write messages
     * @throws IOException
     */
    Writer buildWriter() throws IOException;
}