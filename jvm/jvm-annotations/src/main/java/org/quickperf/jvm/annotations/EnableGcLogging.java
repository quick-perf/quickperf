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

package org.quickperf.jvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>EnableGcLogging</code> annotation enables GC logging.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;EnableGcLogging</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 * <p>
 * The path of the GC log file will be displayed on the
 * console: <p>
 * <code>GC log file: /tmp/QuickPerf-6513460244229078013/gc.log</code><p>
 *
 * This file can be analysed with the help of a GC log analyzer:<p>
 * - <a href="https://github.com/chewiebug/GCViewer">GCViewer</a>,<p>
 * - <a href="https://gceasy.io/">GCeasy</a>,<p>
 * - ...
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface EnableGcLogging {
}
