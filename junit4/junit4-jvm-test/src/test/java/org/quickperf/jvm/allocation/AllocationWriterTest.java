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
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.quickperf.writer.WriterFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class AllocationWriterTest {

    private static final String FILE_PATH = findTargetPath() + "allocation-results.txt";

    private static String findTargetPath() {
        Path targetDirectory = Paths.get("target");
        return targetDirectory.toFile().getAbsolutePath();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ClassUnderTest {

        @MeasureHeapAllocation(writerFactory = FileWriterBuilder.class, format = "[QUICK PERF LOCAL TEST] Measured heap allocation for ArrayList: %s\n")
        @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
        @Test
        public void allocate_array_list_with_size_100() {
            ArrayList<Object> data = new ArrayList<>(100);
        }

        public static class FileWriterBuilder implements WriterFactory {

            @Override
            public Writer buildWriter() throws IOException {
                return new FileWriter(FILE_PATH);
            }
        }

    }

    @Test public void
    should_write_allocation_measure_into_a_file() {

        // GIVEN
        Class<ClassUnderTest> testClass = ClassUnderTest.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();
        assertThat(new File(FILE_PATH)).hasContent("[QUICK PERF LOCAL TEST] Measured heap allocation for ArrayList: 440.0 bytes");

    }

}
