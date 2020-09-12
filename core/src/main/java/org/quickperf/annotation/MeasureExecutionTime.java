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

package org.quickperf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>MeasureExecutionTime</code> annotation measures the execution time of the test method.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;MeasureExecutionTime</b>
 *      public void executeLongAPIcall() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * QuickPerf will give the following feedback on the console:<p> [QUICK PERF] Execution time of the test method: 5 s 289
 * ms (5 289 245 600 ns)
 *
 * <br><br>
 * <h3>Note:</h3>
 * Be cautious with time measurement results. It is a rough and first level result. Data has no meaning below the
 * ~second/millisecond. JIT warm-up, GC, or <a href="https://loonytek.com/2020/01/20/long-jvm-pauses-without-gc/">safe
 * points</a> can impact the measure and its reproducibility. We recommend <a href="https://openjdk.java.net/projects/code-tools/jmh/">JMH</a>
 * to do more in-depth experiments.
 *
 * @see ExpectMaxExecutionTime
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MeasureExecutionTime {
}
