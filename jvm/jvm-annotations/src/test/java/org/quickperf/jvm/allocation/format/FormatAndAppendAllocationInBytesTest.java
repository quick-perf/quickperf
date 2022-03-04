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
package org.quickperf.jvm.allocation.format;

import org.junit.Test;
import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatAndAppendAllocationInBytesTest {

    private final ByteAllocationMeasureFormatter formatter = ByteAllocationMeasureFormatter.INSTANCE;

    @Test public void
    should_format_bytes() {

        // GIVEN
        long lessThanAKilobyte = 1023;
        Allocation allocation = Allocation.ofBytes(lessThanAKilobyte);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1023 bytes");

    }

    @Test public void
    should_format_bytes_to_kilo_bytes() {

        // GIVEN
        long exactlyOneKilobyte = 1024;
        Allocation allocation = Allocation.ofBytes(exactlyOneKilobyte);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Kilo bytes (1 024 bytes)");

    }

    @Test public void
    should_format_bytes_to_100_0_kilo_bytes() {

        // GIVEN
        long oneHundredKiloBytes = 100 * 1024;
        Allocation allocation = Allocation.ofBytes(oneHundredKiloBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.00 Kilo bytes (102 400 bytes)");

    }

    @Test public void
    should_format_bytes_to_mega_byte() {

        // GIVEN
        long oneMegaBytes = 1024 * 1024;
        Allocation allocation = Allocation.ofBytes(oneMegaBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Mega bytes (1 048 576 bytes)");

    }

    @Test public void
    should_format_bytes_to_100_0_mega_bytes() {

        // GIVEN
        long oneHundredMegaBytes = 100 * 1024 * 1024;
        Allocation allocation = Allocation.ofBytes(oneHundredMegaBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.00 Mega bytes (104 857 600 bytes)");

    }

    @Test public void
    should_format_bytes_to_1_0_giga_byte() {

        // GIVEN
        long oneGigaBytes = 1024 * 1024 * 1024;
        Allocation allocation = Allocation.ofBytes(oneGigaBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Giga bytes (1 073 741 824 bytes)");

    }

    @Test public void
    should_format_bytes_to_10_0_giga_bytes() {

        // GIVEN
        long tenGigaBytes = 10L * 1024 * 1024 * 1024;
        Allocation allocation = Allocation.ofBytes(tenGigaBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("10.00 Giga bytes (10 737 418 240 bytes)");

    }

    @Test public void
    should_format_bytes_to_100_0_giga_bytes() {

        // GIVEN
        long oneHundredGigaBytes = 100L * 1024 * 1024 * 1024;
        Allocation allocation = Allocation.ofBytes(oneHundredGigaBytes);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.00 Giga bytes (107 374 182 400 bytes)");

    }

    @Test public void
    should_format_giga_bytes_allocation() {

        // GIVEN
        Allocation allocation = new Allocation(1D, AllocationUnit.GIGA_BYTE);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Giga bytes (1 073 741 824 bytes)");

    }

    @Test public void
    should_format_kilo_bytes_to_megabytes() {

        // GIVEN
        Allocation allocation = new Allocation(1024D, AllocationUnit.KILO_BYTE);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Mega bytes (1 048 576 bytes)");

    }

    @Test public void
    should_format_kilo_bytes_to_giga_bytes() {

        // GIVEN
        Allocation allocation = new Allocation(1024 * 1024D, AllocationUnit.KILO_BYTE);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Giga bytes (1 073 741 824 bytes)");

    }

    @Test public void
    should_convert_mega_bytes_to_giga_bytes() {

        // GIVEN
        Allocation allocation = new Allocation(1024D, AllocationUnit.MEGA_BYTE);

        // WHEN
        String formattedAllocation = formatter.formatAndAppendAllocationInBytes(allocation);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.00 Giga bytes (1 073 741 824 bytes)");

    }

}
