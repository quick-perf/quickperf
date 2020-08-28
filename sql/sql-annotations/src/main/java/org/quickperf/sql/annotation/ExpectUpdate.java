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

package org.quickperf.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectUpdate</code> annotation verifies the number of executed update statements corresponds to the
 * specified value.
 * <p>
 * <h4>Example:</h4>
 * <pre>
 *      <b>&#064;ExpectUpdate(1)</b>
 *      public void execute_one_update() {
 *          <code>..</code>
 *      }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExpectUpdate {

    /**
     * Specifies a <code>value</code> (integer) to cause test method to fail if the number of update
     * statements is not equal. Note that if left empty, the assumed value will be one.
     */
    int value() default 1;

}
