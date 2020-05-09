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

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface MeasureHeapAllocation {

    /**
     * Provides the format used to print the measured heap allocation on the console. This format
     * will be called with a preformatted allocation as a String. So the only element you can
     * use in this format is <code>%s</code>.
     *
     * The default value is <code>[QUICK PERF] Measured heap allocation (test method thread): %s</code>
     *
     * @return
     */
    String format() default "[QUICK PERF] Measured heap allocation (test method thread): %s\n";
}