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
package org.quickperf.sql.annotation;

import org.quickperf.writer.DefaultWriterFactory;
import org.quickperf.writer.WriterFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>AnalyzeSql</code> annotation builds an analysis report of the SQL executed during the test method execution.
 * <p>
 * Displayed information is:
 * <ul><li>Number of total JDBC executions,</li>
 * <li>Longest execution time of all JDBC executions,</li>
 * <li>Number of queries for each type (CRUD). Please note that PLSQL like statements will not be displayed.</li>
 * <li>Alerts and or hints regarding queries syntax (usage of wildcards in select statements, N + 1 one issue etc ... See
 * <a href="https://github.com/quick-perf/doc/wiki/SQL-annotations#configure-global-annotations">Quickperf wiki)</a>,</li>
 * <li>Sql queries.</li>
 * </ul><p>
 * This annotation accepts a Writer class (must implement WriterFactory interface) to allow writing to the desired output.
 * See example below for file export.
 * <br><br>
 * <h3>Example:</h3>
 * <pre><code>
 *  // Helper class used to return a Writer class
 *  public static class FileWriterBuilder implements WriterFactory {
 *
 *     <b>&#064;Override</b>
 *     public Writer buildWriter() throws IOException {
 *         return new FileWriter(desired-path-to-exported-file);
 *     }
 *  }
 *
 *  <b>&#064;AnalyzeSql(writerFactory = FileWriterBuilder.class)</b>
 *  public void select() {
 *           <code>..</code>
 *      });
 *  }
 * </code></pre>
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AnalyzeSql {
    /**
     * In case no argument is provided, the Sql report will be displayed in the console thanks to {@link DefaultWriterFactory}.
     */
    Class<? extends WriterFactory> writerFactory() default DefaultWriterFactory.class;

}
