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
import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class ShortFormatAllocationTest {

    private final ByteAllocationMeasureFormatter formatter = ByteAllocationMeasureFormatter.INSTANCE;

    @Test public void
    should_format_bytes() {
        assertThat(formatter.shortFormat(1023, 2)).isEqualTo("1023 B");
    }

    @Test public void
    should_format_kilo_bytes() {
        assertThat(formatter.shortFormat(1024, 2)).isEqualTo("1.00 KiB");
    }

    @Test
    public void should_format_kilo_bytes_with_two_decimals() {
        assertThat(formatter.shortFormat(1550, 2)).isEqualTo("1.51 KiB");
    }

    @Test
    public void should_format_mega_bytes_with_two_decimals() {
        long allocationValue = (1024 * 1024) + 20_000;
        assertThat(formatter.shortFormat(allocationValue, 2)).isEqualTo("1.02 MiB");
    }

    @Test
    public void should_format_mega_bytes_with_no_decimal() {
        long allocationValue = (1024 * 1024) + 20_000;
        assertThat(formatter.shortFormat(allocationValue, 0)).isEqualTo("1 MiB");
    }

    @Test
    public void should_format_giga_bytes_with_two_decimals() {
        long allocationValue = (1024 * 1024 *  1024) + 20_000_000;
        assertThat(formatter.shortFormat(allocationValue, 2)).isEqualTo("1.02 GiB");
    }

    @Test
    public void should_format_one_mega_byte() {
        assertThat(formatter.shortFormat(1024 * 1024, 2)).isEqualTo("1.00 MiB");
    }

    @Test
    public void should_format_one_giga_byte() {
        assertThat(formatter.shortFormat(1024 * 1024 * 1024, 2)).isEqualTo("1.00 GiB");
    }

}
