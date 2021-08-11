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

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ProfileConnection {

    Level level() default Level.INFO;

    boolean beforeAndAfterTestMethodExecution() default false;

    boolean displayStackTrace() default false;

    boolean filterStackTrace() default true;

    short stackDepth() default -1;

    Class<? extends WriterFactory> writerFactory() default DefaultWriterFactory.class;

}

