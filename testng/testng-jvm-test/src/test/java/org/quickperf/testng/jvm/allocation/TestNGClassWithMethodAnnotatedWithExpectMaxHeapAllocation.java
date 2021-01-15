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

package org.quickperf.testng.jvm.allocation;

import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.JvmOptions;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class TestNGClassWithMethodAnnotatedWithExpectMaxHeapAllocation {

    @ExpectMaxHeapAllocation(value = 439, unit = AllocationUnit.BYTE)
    @JvmOptions("-XX:+UseCompressedOops -XX:+UseCompressedClassPointers")
    @Test
    public void array_list_with_size_100_should_allocate_440_bytes() {
       ArrayList<Object> data = new ArrayList<>(100);
    }

}
