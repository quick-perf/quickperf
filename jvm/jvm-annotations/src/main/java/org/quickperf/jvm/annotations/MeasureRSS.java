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
 * The <code>MeasureRSS</code> annotation measures the <a href="https://en.wikipedia.org/wiki/Resident_set_size">Resident
 * Set Size</a> (RSS) with this annotation.
 * <p>
 * <h4>Example:</h4>
 * <pre>
 *      <b>&#064;MeasureRSS
 *      &#064;Test</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 * <p>
 * <p>
 * QuickPerf will give the following type of feedback on the console:<p> [QUICK PERF] Measured RSS (process 5227): 46.64
 * Mega bytes (48 902 144 bytes)
 *
 * <h4>Note:</h4>
 * Today this annotation only woks on Linux. You can work on this <a href="https://github.com/quick-perf/quickperf/issues/56">issue</a>
 * to make the RSS annotations work on MacOS.
 *
 * @see ExpectMaxRSS
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MeasureRSS {
}
