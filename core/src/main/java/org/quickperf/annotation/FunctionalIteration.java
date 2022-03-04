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
package org.quickperf.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>FunctionalIteration</code> annotation shows we are implementing the functional, not performance-related
 * behavior. A typical use case is the development of a new feature. First, this annotation allows us to focus on the
 * implementation of functional behavior by disabling global annotations. In a second step, we remove it to re-enable
 * global annotations. Later on, we may also add other QuickPerf annotations on the test method.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;FunctionalIteration</b>
 *      <b>&#064;DisableSameSelectTypesWithDifferentParamValues</b>
 *      public void execute() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * @see DisableQuickPerf
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface FunctionalIteration {

}
