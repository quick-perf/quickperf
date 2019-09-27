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
import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import static org.assertj.core.api.Assertions.assertThat;

public class ByteAllocationMeasureFormatterTest {

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;
    
    @Test public void
    should_convert_bytes_to_100_0_kilo_bytes() {

        // GIVEN
        double tenGigaByteAllocationValue = 100 * 1024;
        Allocation allocationInBytes = new Allocation(tenGigaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.0 Kilo bytes (102 400 bytes)");

    }

    @Test public void
    should_convert_bytes_to_one_mega_byte() {

        // GIVEN
        double oneMegaByteAllocationValue = 1 * Math.pow(1024, 2);
        Allocation allocationInBytes = new Allocation(oneMegaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.0 Mega bytes (1 048 576 bytes)");

    }

    @Test public void
    should_convert_bytes_to_100_0_mega_bytes() {

        // GIVEN
        double tenMegaByteAllocationValue = 100 * Math.pow(1024, 2);
        Allocation allocationInBytes = new Allocation(tenMegaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.0 Mega bytes (104 857 600 bytes)");

    }

    @Test public void
    should_convert_bytes_to_1_0_giga_byte() {

        // GIVEN
        double oneGigaByteAllocationValue = Math.pow(1024, 3);
        Allocation allocationInBytes = new Allocation(oneGigaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.0 Giga bytes (1 073 741 824 bytes)");

    }

    @Test public void
    should_convert_bytes_to_10_0_giga_bytes() {

        // GIVEN
        double tenGigaByteAllocationValue = 10 * Math.pow(1024, 3);
        Allocation allocationInBytes = new Allocation(tenGigaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("10.0 Giga bytes (10 737 418 240 bytes)");

    }

    @Test public void
    should_convert_bytes_to_100_0_giga_bytes() {

        // GIVEN
        double tenGigaByteAllocationValue = 100 * Math.pow(1024, 3);
        Allocation allocationInBytes = new Allocation(tenGigaByteAllocationValue, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("100.0 Giga bytes (107 374 182 400 bytes)");

    }

    @Test public void
    should_convert_bytes_to_1023_bytes_without_byte_suffix() {

        // GIVEN
        double lessThanAKilobyte = 1023;
        Allocation allocationInBytes = new Allocation(lessThanAKilobyte, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1023.0 bytes");

    }

    @Test public void
    should_convert_bytes_to_1024_bytes_with_byte_suffix() {

        // GIVEN
        double exactlyOneKilobyte = 1024;
        Allocation allocationInBytes = new Allocation(exactlyOneKilobyte, AllocationUnit.BYTE, "");

        // WHEN
        String formattedAllocation = byteAllocationMeasureFormatter.format(allocationInBytes);

        // THEN
        assertThat(formattedAllocation).isEqualTo("1.0 Kilo bytes (1 024 bytes)");

    }

}
