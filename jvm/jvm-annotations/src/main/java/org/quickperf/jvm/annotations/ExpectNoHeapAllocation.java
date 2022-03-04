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
package org.quickperf.jvm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectNoHeapAllocation</code> annotation makes the test fail if the test thread allocates on the heap.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;ExpectNoHeapAllocation
 *      &#064;Test</b>
 *      public void randomMethod() {
 *          <code>...</code>
 *      }
 * </pre>
 *
 * @see HeapSize
 * @see MeasureHeapAllocation
 * @see ExpectMaxHeapAllocation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectNoHeapAllocation {
}
