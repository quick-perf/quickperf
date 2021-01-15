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
 * The <code>DisableLikeWithLeadingWildcard</code> annotation verifies that SQL statements do not contain a LIKE with
 * a leading wildcard (% or _). If so, the test will fail.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;DisableLikeWithLeadingWildcard</b>
 *      public void execute_select_who_started_with_like_wildcard() {
 *          <code>..</code>
 *      }
 * </pre>
 *
 * <br><br>
 * <h3>Note:</h3>
 * <a href="https://use-the-index-luke.com/sql/where-clause/searching-for-ranges/like-performance-tuning"><u>This
 * article</u></a>
 * explains why a LIKE with a leading wildcard could be a bad idea for performance. A code sent to the database with
 * a like operator with leading wildcard may be fast in a test having a few data but very slow with the data volume of a
 * production environment.
 *
 * @see EnableLikeWithLeadingWildcard
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisableLikeWithLeadingWildcard {
}
