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

public class AllocationComparisonTest {
    
    @Test public void
    one_kilo_byte_allocation_should_be_greater_than_one_byte_allocation() {

        Allocation oneByte = Allocation.ofBytes(1);
        Allocation oneKiloByte = new Allocation(1D, AllocationUnit.KILO_BYTE);

        assertThat(oneKiloByte.compareTo(oneByte)).isPositive();

    }

    @Test public void
    one_mega_byte_allocation_should_be_greater_than_one_kilo_byte_allocation() {

        Allocation oneByte = new Allocation(1D, AllocationUnit.KILO_BYTE);
        Allocation oneKiloByte = new Allocation(1D, AllocationUnit.MEGA_BYTE);

        assertThat(oneKiloByte.compareTo(oneByte)).isPositive();

    }

    @Test public void
    two_thousand_mega_byte_allocation_should_be_greater_than_one_giga_byte_allocation() {

        Allocation oneGigaByte = new Allocation(1D, AllocationUnit.GIGA_BYTE);
        Allocation twoThousandMegaByte = new Allocation(2000D, AllocationUnit.MEGA_BYTE);

        assertThat(twoThousandMegaByte.compareTo(oneGigaByte)).isPositive();

    }

    @Test public void
    one_hundred_giga_bytes_should_be_greater_than_one_byte() {

        Allocation oneHundredGigaByte = new Allocation(100D, AllocationUnit.GIGA_BYTE);
        Allocation oneByte = Allocation.ofBytes(1);

        assertThat(oneHundredGigaByte.compareTo(oneByte)).isPositive();

    }

    @Test public void
    one_byte_should_be_less_than_hundred_giga_bytes() {

        Allocation oneHundredGigaByte = new Allocation(100D, AllocationUnit.GIGA_BYTE);
        Allocation oneByte = Allocation.ofBytes(1);

        assertThat(oneByte.compareTo(oneHundredGigaByte)).isNegative();

    }

    @Test public void
    same_allocation() {

        Allocation oneByte1 = Allocation.ofBytes(1);
        Allocation oneByte2 = new Allocation(1D, AllocationUnit.BYTE);

        assertThat(oneByte1.compareTo(oneByte2)).isZero();
        assertThat(oneByte2.compareTo(oneByte1)).isZero();

    }

}