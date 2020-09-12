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
 * The <code>DisplaySqlOfTestMethodBody</code> annotation displays the SQL statements in the console during the
 * execution of the test method body.
 * <p>
 * Compared to {@link DisplaySql}, this annotation <u><b>does not</b></u> display SQL statements before (JUnit 4: @Before
 * &#064;BeforeClass) and after (JUnit 4: &#064;After, &#064;AfterClass) the execution of the test method body. <br>
 * <p>
 * It is not recommended to commit your test with this annotation. Indeed, the SQL statements would pollute the logs
 * and may slow down the continuous integration build.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;DisplaySqlOfTestMethodBody</b>
 *      public void obscure_sql_query_execution() {
 *          <code>..</code>
 *      }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
public @interface DisplaySqlOfTestMethodBody {
}
