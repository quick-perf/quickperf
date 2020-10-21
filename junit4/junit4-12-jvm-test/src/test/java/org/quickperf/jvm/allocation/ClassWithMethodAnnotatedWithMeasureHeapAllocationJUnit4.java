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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.quickperf.writer.WriterFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@RunWith(QuickPerfJUnitRunner.class)
public class ClassWithMethodAnnotatedWithMeasureHeapAllocationJUnit4 {

    @MeasureHeapAllocation
    @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
    // Allocation value depends on UseCompressedOops and UseCompressedClassPointers.
    // QuickPerf works with JDK >= 7u40 where UseCompressedOops is enabled by default.
    // UseCompressedClassPointers was introduced in JDK 8 and is
    // enabled by default.
    // No annotation: this test should log messages to System.out
    @Test
    public void array_list_with_size_100_should_allocate_440_bytes() {
        // java.util.ArrayList: 24 bytes
        //            +
        //  Object[]: 16 + 100 x 4 = 416
        //       = 440 bytes
        ArrayList<Object> data = new ArrayList<>(100);
    }

    // Annotation and correct class: this test should log messages to the file
    @MeasureHeapAllocation(writerFactory = PrintWriterBuilder.class, format = "[QUICK PERF LOCAL TEST] Measured heap allocation for ArrayList: %s\n")
    @Test
    public void a_test_that_logs_to_a_file() {
        ArrayList<Object> data = new ArrayList<>(100);
    }

    // Annotation and incorrect class: shoud fall back to System.out and log an error message
    @MeasureHeapAllocation(writerFactory = BuggyPrintWriterBuilder.class, format = "[QUICK PERF LOCAL TEST] Measured heap allocation for ArrayList: %s\n")
    @Test
    public void a_test_that_should_logs_to_a_file_but_falls_back_to_System_out() {
        ArrayList<Object> data = new ArrayList<>(100);
    }

    // No annotation: should log to System.out
    @MeasureHeapAllocation(format = "[QUICK PERF LOCAL TEST] Measured heap allocation LinkedList: %s\n")
    @Test
    public void a_test_that_should_logs_to_System_out() {
        List<Object> data = new LinkedList<>();
    }

    @MeasureHeapAllocation(writerFactory = PrintWriterBuilder.class)
    @Test
    public void a_third_dumb_test() {
        Set<Object> data = new HashSet<>();
    }


    public static class PrintWriterBuilder implements WriterFactory {

        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter("files/quickperf-allocation-results.txt");
        }
    }

    public static class BuggyPrintWriterBuilder implements WriterFactory {

        public BuggyPrintWriterBuilder(String dumbMessage) {
            // just a constructor that will raise an exception
        }

        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter("files/quickperf-allocation-results.txt");
        }
    }

}
