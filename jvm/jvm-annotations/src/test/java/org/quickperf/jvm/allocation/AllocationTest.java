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
package org.quickperf.jvm.allocation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationTest {


    @Test
    public void get_value_of_one_giga_byte() {

        long numberOfBytes = 1024L * 1024 * 1024;

        Allocation oneGigaByte = Allocation.ofBytes(numberOfBytes);

        assertThat(oneGigaByte.getValueInBytes()).isEqualTo(numberOfBytes);

    }

    @Test
    public void get_value_of_five_hundred_giga_byte() {

        long numberOfBytes = 500L * 1024 * 1024 * 1024;

        Allocation oneGigaByte = Allocation.ofBytes(numberOfBytes);

        assertThat(oneGigaByte.getValueInBytes()).isEqualTo(numberOfBytes);

    }

}
