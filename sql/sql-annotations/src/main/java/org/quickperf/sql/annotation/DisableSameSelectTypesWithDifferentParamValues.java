/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>DisableSameSelectTypesWithDifferentParamValues</code> annotation verifies that same SELECT statements are
 * not executed with different parameter values. If so, the test will fail.
 *
 * This annotation can reveal some N+1 selects.
 *
 * <br><br>
 * <h3>Example:</h3>
 * A test using this annotation and generating the statements below will fail.
 * <pre>
*select
*   team0_.id as id1_1_0_,
*   team0_.name as name2_1_0_
*from
*   Team team0_
*where team0_.id=?
*
*Params:[(1)]
* </pre>
* <pre>
*select
*   team0_.id as id1_1_0_,
*   team0_.name as name2_1_0_
*from
*   Team team0_
*where team0_.id=?
*
*Params:[(2)]
* </pre>
*
* @see EnableSameSelectTypesWithDifferentParamValues
*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableSameSelectTypesWithDifferentParamValues {
}
