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
package org.quickperf.jvm.annotations;

import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>MeasureHeapAllocation</code> annotation measures the heap allocation of the test method thread.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MeasureHeapAllocation {

    String QUICK_PERF_MEASURED_HEAP_ALLOCATION_DEFAULT_FORMAT = "[QUICK PERF] Measured heap allocation (test method thread): %s\n";

    /**
     * Provides the format used to print the measured heap allocation on the console. This format
     * will be called with a preformatted allocation as a String. So the only element you can
     * use in this format is <code>%s</code>.
     *
     * The default value is <code>[QUICK PERF] Measured heap allocation (test method thread): %s</code>
     *
     * @return the format used to print the measured heap allocation on the console
     */
    String format() default QUICK_PERF_MEASURED_HEAP_ALLOCATION_DEFAULT_FORMAT;

    /**
     * <p>Allows you to provide a way to build a <code>Writer</code> instance to print your messages.
     * The <code>WriterFactory</code> class is used to built this <code>Writer</code>.
     * </p>
     * <p>This <code>WriterFactory</code> class is constructed using reflection, so it should have an empty
     * constructor. If it does not an exception will be raised and the default <code>Writer</code> will be used.
     * </p>
     * <p>The default value <code>DefaultWriterFactory</code> builds a <code>Writer</code> that writes to
     * <code>System.out</code>. In case an exception is raised in the use of a provided factory, the system
     * falls back on this default value.</p>
     *
     * @return a class that implements the <code>WriterFactory</code> interface
     * @see org.quickperf.writer.WriterFactory
     */
    Class<? extends WriterFactory> writerFactory()
            default DefaultWriterFactory.class;
}