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
package org.quickperf.jvm.jfr.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectNoJvmIssue</code> annotation profiles the JVM with <code>JDK Flight Recorder (JFR)</code>. Based on profiling, the annotation evaluates <a href="http://hirt.se/blog/?p=920"><i>JMC rules</i></a>
 *  (heuristics). For each rule, a score is attributed. The maximum score value is 100. The test will fail if one rule has a score greater than this expected (by
 * default 60). Things like significant primitives to object conversions can be detected:
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;ExpectNoHeapAllocation
 *      &#064;Test</b>
 *      public void randomMethod() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * <pre>
 * QuickPerf will give the following type of feedback on the console:
 *
 * <code>[PERF] JMC rules are expected to have score less than &lt;50&gt;.
 * Rule: Primitive To Object Conversion
 * Severity: INFO
 * Score: 74
 * Message: 79 % of the total allocation (45,6 MiB) is caused by conversion from primitive types to object types.
 * The most common object type that primitives are converted into is
 * 'java.lang.Integer', which causes 45,6 MiB to be allocated. The most common call site is
 * 'org.quickperf.jvm.jmc.JmcJUnit4Tests$ClassWithFailingJmcRules$IntegerAccumulator.accumulateInteger(int):40'.</code>
 * </pre>
 * <pre>
 * With this annotation you can also detect that most of the time is spent to do garbage collection in your test. If you
 * have the following message in the console:
 * <code>
 * Rule: Stackdepth Setting Severity: WARNING Score: 97 Message: Some stack traces were truncated in this recording.
 * </code>
 * Then you can increase the stack depth value in this way:
 * <b>&#064;JvmOptions("-XX:FlightRecorderOptions=stackdepth=128")</b>
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectNoJvmIssue {

    /**
     * Score value.
     */
    int score() default 60;

}
