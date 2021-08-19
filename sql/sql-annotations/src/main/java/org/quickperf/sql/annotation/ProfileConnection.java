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

package org.quickperf.sql.annotation;

import org.quickperf.sql.connection.Level;
import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ProfileConnection</code> annotation profiles the database connection.
 * <p>
 * The annotation profiles the calls to the {@link java.sql.Connection} methods and to the {@link javax.sql.DataSource}
 * <code>getConnection()</code> method.
 * @see ExpectNoConnectionLeak
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ProfileConnection {


    /**
     * Allows defining the profiling level. The default value is <code>INFO</code>.
     * With a <code>TRACE</code> level, the annotation profiles more {@link java.sql.Connection} methods.
     * @return The profiling level value
     */
    Level level() default Level.INFO;

    /**
     * Allows profiling the {@link java.sql.Connection} before and after the test method execution.
     * The default behavior is not to profile the database connection before and after the test method execution.
     * @return True if the annotation has to profile the database connection before and after the test method execution.
     */
    boolean beforeAndAfterTestMethodExecution() default false;

    /**
     * Allows displaying the stack traces of the profiled {@link java.sql.Connection} methods. The annotation does not display the stack traces by default.
     * @return True if the annotation has to display the stack traces of the profiled {@link java.sql.Connection} methods
     */
    boolean displayStackTrace() default false;

    /**
     * If the stack trace display is enabled, the <code>filterStackTrace</code> element allows removing elements from the stack traces coming from <code>QuickPerf</code>, <code>JUnit 4</code>, <code>JUnit 5</code>, and <code>TestNG</code>.
     * By default, the annotation filters the stack trace elements.
     *
     * @return True id the annotation has to remove <code>QuickPerf</code>, <code>JUnit 4</code>,
     * <code>JUnit 5</code>, and <code>TestNG</code> elements from the stack traces.
     */
    boolean filterStackTrace() default true;

    /**
     *If the stack trace display is enabled, the <code>stackDepth</code> element allows configuring the number of elements to display on the stack traces. With a <code>-1</code> value, the annotation does not limit the stack depth.
     * @return The number of elements to display on the stack traces
     */
    short stackDepth() default -1;

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
    Class<? extends WriterFactory> writerFactory() default DefaultWriterFactory.class;

}

