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

import java.io.PrintWriter;
import java.io.Writer;

public class PrintWriterBuilder {

    public static final PrintWriterBuilder INSTANCE = new PrintWriterBuilder();

    private PrintWriterBuilder() { }

    public PrintWriter buildPrintWriterFrom(Class<? extends WriterFactory> writerFactoryClass) {
        PrintWriter pw;
        try {
            WriterFactory writerFactory = writerFactoryClass.getConstructor().newInstance();
            Writer writer = writerFactory.buildWriter();
            pw = new PrintWriter(writer);
        } catch (Exception e) {
            System.out.printf("Unexpected exception while building the writer factory [%s]\n", writerFactoryClass.getName());
            e.printStackTrace(System.out);
            System.out.println("Messages will be sent to System.out");
            pw = DefaultWriterFactory.SystemOutPrintWriterInstance.INSTANCE.getSystemOutPrintWriter();
        }
        return pw;
    }

}
