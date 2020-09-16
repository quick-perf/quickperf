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

import org.quickperf.annotation.FunctionalIteration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>JvmOptions</code> annotation makes the test executed in a specific JVM having the given JVM options.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;JvmOptions("-Xlog:gc*")</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 * <p>
 *
 * @see <a href="https://chriswhocodes.com/vm-options-explorer.html">This</a> tool developed by Chris Newland can be
 * used to explore the available JVM options.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface JvmOptions {

    /**
     * JVM arguments.
     */
    String value() default "";

}
