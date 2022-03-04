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
 * The <code>DisplayAppliedAnnotations</code> annotation displays all applied QuickPerf annotations. Being from global,
 * class or method scope.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;DisplayAppliedAnnotations</b>
 *      <b>&#064;RunWith(QuickPerfJUnitRunner.class)</b>
 *      public static class AClassWithVariousCoolAnnotations extends SqlTest
 *
 *           <b>&#064;Test</b>
 *           <b>&#064;DisableLikeWithLeadingWildcard</b>
 *           <b>&#064;ExpectMaxSelect(5)</b>
 *           <b>&#064;ProfileJvm</b>
 *           public void execute_select_who_started_with_like_wildcard() {
 *              <code>..</code>
 *           }
 *
 *       }
 * </pre>
 * QuickPerf will give the following feedback on the console:<p> [QUICK PERF] Applied annotations:
 * <b>&#064;DisableLikeWithLeadingWildcard</b>, <b>&#064;ExpectMaxSelect(value=5)</b>,
 * <b>&#064;ProfileJvm</b>, <b>&#064;DebugQuickPerf</b>
 *
 * <br><br>
 * <h3>Note:</h3>
 * QuickPerf won't display <b>&#064;DisplayAppliedAnnotations</b> as part of the applied annotations in the output
 * message.
 *
 * @see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 * scopes</i></a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DisplayAppliedAnnotations {
}
