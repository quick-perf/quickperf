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
package org.quickperf.time;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatExecutionTimeAndAppendNsTest {

    private final ExecutionTimeFormatter formatter = ExecutionTimeFormatter.INSTANCE;

    @Test public void
    should_format_nano_seconds() {
        ExecutionTime executionTime = new ExecutionTime(100, TimeUnit.NANOSECONDS);
        assertThat(formatter.formatAndAppendNanoSeconds(executionTime))
                .isEqualTo("100 ns");
    }

    @Test public void
    should_format_three_thousand_and_two_nano_seconds() {
        assertThat(formatter.formatAndAppendNanoSeconds(new ExecutionTime(3_002, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 µs 2 ns (3 002 ns)");
    }

    @Test public void
    should_format_to_minutes_seconds_with_nanoseconds_appended() {
        long oneBillion = (long) Math.pow(10, 9);
        assertThat(formatter.formatAndAppendNanoSeconds(new ExecutionTime(((3 * 60) + 12) * oneBillion + 27, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 m 12 s 0 ms (192 000 000 027 ns)");
    }

}
