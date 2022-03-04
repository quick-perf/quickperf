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

import org.quickperf.jvm.gc.GC;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>UseGC</code> annotation makes the test executed in a specific JVM with the given Garbage Collector (GC).<p>
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;UseGC(GC.EPSILON_GC)</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * @see org.quickperf.jvm.gc.GC
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseGC {

    /**
     * Garbage Collector type.
     */
    GC value() default GC.DEFAULT;

}
