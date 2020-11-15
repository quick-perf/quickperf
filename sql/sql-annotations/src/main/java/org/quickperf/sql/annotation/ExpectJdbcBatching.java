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
 * The <code>ExpectJdbcBatching</code> annotation that insert, delete, and update statements
 * are processed through JDBC batches of <code>batchSize</code> elements.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;ExpectJdbcBatching(batchSize = 30)</b>
 *      public void insert_using_jdbc_batching_system(){
 *          <code>..</code>
 *      }
 * </pre>
 *
 * <br><br>
 * <h3>Note:</h3>
 * YYou may sometimes think that you are using JDBC batching, but in fact, you are not:
 * <ul>
 * <li>
 * <a href="https://abramsm.wordpress.com/2008/04/23/hibernate-batch-processing-why-you-may-not-be-using-it-even-if-you-think-you-are/"><i>Hibernate
 * batch processing why you may not be using it even if you think you are</i></a>
 * </li>
 * <li>
 * <a href="https://stackoverflow.com/questions/27697810/hibernate-disabled-insert-batching-when-using-an-identity-identifier"><i>Hibernate disabled insert batching when using an identity identifier</i></a>
 * </li>
 * </ul>
 *
 * @see <a href="https://blog.jooq.org/2017/12/18/the-cost-of-jdbc-server-roundtrips/"><i>The cost of JDBC Server roundtrips.</i></a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectJdbcBatching {

    /**
     * Specifies a <code>batchSize</code> (integer) to cause the test method to fail if the used batch size is not equal. A zero batch size means that JDBC batching is <b><u>disabled</u></b>. With no given batch size value, the
     * annotation will still check that insert, delete and update statements are processed through JDBC batches (but the
     * annotation will not check the batch size).
     */
    int batchSize() default -1;

}
