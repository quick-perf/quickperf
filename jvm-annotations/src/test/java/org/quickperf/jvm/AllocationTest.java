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

package org.quickperf.jvm;

import org.junit.Test;
import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationTest {
    
    @Test public void
    one_kilo_byte_allocation_should_be_greater_than_one_byte_allocation() {

        // GIVEN
        Allocation oneByte = new Allocation(1D, AllocationUnit.BYTE);
        Allocation oneKiloByte = new Allocation(1D, AllocationUnit.KILO_BYTE);
        
        // WHEN
        int oneKiloByteComparedToOneByte = oneKiloByte.compareTo(oneByte);

        // THEN
        assertThat(oneKiloByteComparedToOneByte).isPositive();

    }

    @Test public void
    one_mega_byte_allocation_should_be_greater_than_one_kilo_byte_allocation() {

        // GIVEN
        Allocation oneByte = new Allocation(1D, AllocationUnit.KILO_BYTE);
        Allocation oneKiloByte = new Allocation(1D, AllocationUnit.MEGA_BYTE);

        // WHEN
        int oneKiloByteComparedToOneByte = oneKiloByte.compareTo(oneByte);

        // THEN
        assertThat(oneKiloByteComparedToOneByte).isPositive();

    }

    @Test public void
    two_thousand_mega_byte_allocation_should_be_greater_than_one_giga_byte_allocation() {

        // GIVEN
        Allocation oneGigaByte = new Allocation(1D, AllocationUnit.GIGA_BYTE);
        Allocation twoThousandMegaByte = new Allocation(2000D, AllocationUnit.MEGA_BYTE);

        // WHEN
        int twoThousandMegaByteComparedToOneGigaByte = twoThousandMegaByte.compareTo(oneGigaByte);

        // THEN
        assertThat(twoThousandMegaByteComparedToOneGigaByte).isPositive();

    }

}