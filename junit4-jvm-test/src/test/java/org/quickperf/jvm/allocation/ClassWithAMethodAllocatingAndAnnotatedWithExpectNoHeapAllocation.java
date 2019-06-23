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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.jvm.allocation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;

import java.util.ArrayList;

@RunWith(QuickPerfJUnitRunner.class)
public class ClassWithAMethodAllocatingAndAnnotatedWithExpectNoHeapAllocation {

    @ExpectNoHeapAllocation
    // See ClassWithMethodAnnotatedWithMeasureHeapAllocation
    @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
    @Test
    public void method_allocating() {
        ArrayList<Object> data = new ArrayList<>(100);
    }

}
