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
package org.quickperf.testng.jvm.skip;

import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.testng.SkipException;
import org.testng.annotations.Test;

public class TestNGMethodAllocatingAndThenSkippedInASpecificJvm {

    @ExpectMaxHeapAllocation(value = 1, unit = AllocationUnit.MEGA_BYTE)
    @Test
    public void test_method_allocating_3_mega_bytes_and_then_skipped() {
        byte[] allocatedBytes = new byte[3 * 1024 * 1024];
        throw new SkipException("Skipped after allocating 3 MB");
    }

}
