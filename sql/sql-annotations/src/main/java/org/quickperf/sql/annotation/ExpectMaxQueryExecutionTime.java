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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * The <code>ExpectMaxQueryExecutionTime</code> annotation verifies the query execution time is not greater than the
 * specified value. If so, the test will fail.
 * <p>
 * <h4>Example:</h4>
 * <pre>
 *      <b>&#064;ExpectMaxQueryExecutionTime(thresholdInMilliSeconds = 2)</b>
 *      public void execute() {
 *          <code>..</code>
 *      }
 * </pre>
 */

@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectMaxQueryExecutionTime {

    /**
     * Specifies a <code>value</code> (integer) to cause test method to fail if any query has a greater execution time.
     * Note that if left empty, the assumed value will be zero.
     */

    int thresholdInMilliSeconds() default 0;

}
