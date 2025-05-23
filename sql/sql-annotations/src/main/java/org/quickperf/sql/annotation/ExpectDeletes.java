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
package org.quickperf.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectDeletes</code> annotation verifies the number of executed delete statements corresponds to the
 * specified values.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;ExpectDeletes({</b>
 *          <b>&#064;ExpectDelete(comment="Delete user"),</b>
 *          <b>&#064;ExpectDelete(comment="Delete posts",value=2)</b>
 *      <b>})</b>
 *      public void execute_three_delete() {
 *          <code>..</code>
 *      }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ExpectDeletes {

    /**
     * Specifies an array of expected queries.
     */
    ExpectDelete[] value();

}
