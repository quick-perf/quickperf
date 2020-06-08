/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.jvm.allocation;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.io.PrintWriter;
import java.io.Writer;

public class MeasureHeapAllocationPerfVerifier implements VerifiablePerformanceIssue<MeasureHeapAllocation, Allocation> {

    public static final VerifiablePerformanceIssue INSTANCE = new MeasureHeapAllocationPerfVerifier();

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;

    private MeasureHeapAllocationPerfVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(MeasureHeapAllocation annotation, Allocation measuredAllocation) {
        String allocationAsString = byteAllocationMeasureFormatter.formatWithAllocationInBytes(measuredAllocation);
        Class<? extends WriterFactory> writerFactoryClass = annotation.writerFactory();
        try (PrintWriter pw = buildPrintWriterFrom(writerFactoryClass);) {
            pw.printf(annotation.format(), allocationAsString);
        }
        return PerfIssue.NONE;
    }

    private PrintWriter buildPrintWriterFrom(Class<? extends WriterFactory> writerFactoryClass) {
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
