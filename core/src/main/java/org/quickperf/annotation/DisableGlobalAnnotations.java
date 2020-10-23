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
 * The <code>DisableGlobalAnnotations</code> annotation disables QuickPerf annotations having a <u>global</u> scope.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;DisableGlobalAnnotations</b>
 *      <b>&#064;Test</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * @see FunctionalIteration
 * @see DisableQuickPerf
 * @see org.quickperf.config.SpecifiableGlobalAnnotations
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableGlobalAnnotations {

    String comment() default "";

}
