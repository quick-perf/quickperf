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
 * The <code>DisableQueriesWithoutBindParameters</code> annotation ensures the executed queries use bind parameters. The
 * test will fail if parameters are not bind.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;DisableQueriesWithoutBindParameters</b>
 *      public void execute_query_without_bind_parameter() {
 *          <code>..</code>
 *      }
 * </pre>
 *
 * <br><br>
 * <h3>Note:</h3>
 * Keep in mind that bind parameters is an essential feature to prevent SQL injections and can help improve performance:
 * <ul>
 *<li>
 *<a href="https://blogs.oracle.com/sql/improve-sql-query-performance-by-using-bind-variables">Improve SQL query by
 *  * using bind variables (Oracle blog)</a>
 *</li>
 * <li>
 * <a href="https://use-the-index-luke.com/sql/where-clause/bind-parameters">Use the index Luke: parametrized
 * queries</a>.
 *</li>
 * <li>
 * <a href="https://dzone.com/articles/why-sql-bind-variables-are-important-for-performan">Why SQL bind variables are important for performance</a>
 * </li>
 * </ul>
 *
 * @see EnableQueriesWithoutBindParameters
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableQueriesWithoutBindParameters {
}
