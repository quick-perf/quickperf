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

import org.quickperf.jvm.allocation.AllocationUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectMaxRSS</code> annotation will make the test fail if <a href="https://en.wikipedia.org/wiki/Resident_set_size">Resident
 * Set Size</a> (RSS) is greater than expected.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;ExpectMaxRSS(value = 1, unit = AllocationUnit.MEGA_BYTE)
 *      &#064;Test</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 * QuickPerf will give the following type of feedback on the console:<p>
 * <code>[PERF] Expected RSS to be less than 1.00 Mega bytes but is 65.42 Mega bytes (68 599 808 bytes).</code>
 *
 * <br><br>
 * <h3>Note:</h3>
 * Today this annotation only works on Linux. You can work on this <a href="https://github.com/quick-perf/quickperf/issues/56">issue</a>
 * to make the RSS annotations work on MacOS.
 *
 * @see MeasureRSS
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectMaxRSS {

    /**
     * Allocation value.
     */
    double value();

    /**
     * Allocation unit.
     */
    AllocationUnit unit();
}
