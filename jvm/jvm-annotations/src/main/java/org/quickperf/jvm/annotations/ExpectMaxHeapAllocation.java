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

import org.quickperf.jvm.allocation.AllocationUnit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>ExpectMaxHeapAllocation</code> annotation makes the test fail if the heap allocation of the test method thread is
 * greater than expected.
 *
 * <br><br>
 * <h3>Example:</h3>
 * <pre>
 *      <b>&#064;HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
 *      &#064;ExpectMaxHeapAllocation(value = 440, unit = AllocationUnit.BYTE)
 *      &#064;Test</b>
 *      public void array_list_with_size_100_should_allocate_440_bytes() {
 *          ArrayList&lt;Objec&gt; data = new ArrayList&lt;&gt;(100);
 *      }
 * </pre>
 *
 * @see HeapSize
 * @see MeasureHeapAllocation
 * @see ExpectNoHeapAllocation
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ExpectMaxHeapAllocation {

    /**
     * Allocation value.
     */
    double value();

    /**
     * Allocation unit.
     */
    AllocationUnit unit();

}
