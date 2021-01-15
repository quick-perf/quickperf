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

package org.quickperf.time;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatExecutionTimeTest {

    private final ExecutionTimeFormatter formatter = ExecutionTimeFormatter.INSTANCE;

    @Test public void
    should_format_one_hundred_nano_seconds() {
        assertThat(formatter.format(new ExecutionTime(100, TimeUnit.NANOSECONDS)))
                .isEqualTo("100 ns");
    }

    @Test public void
    should_format_three_thousand_and_two_nano_seconds() {
        assertThat(formatter.format(new ExecutionTime(3_002, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 µs 2 ns");
    }

    @Test public void
    should_format_three_millions_nanoseconds_to_milli_seconds_and_micro_seconds() {
        assertThat(formatter.format(new ExecutionTime(3_099_999, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 ms 100 µs");
    }

    @Test public void
    should_format_three_millions_nanoseconds_to_milli_seconds_and_zero_micro_second() {
        assertThat(formatter.format(new ExecutionTime(3_999_999, TimeUnit.NANOSECONDS)))
                .isEqualTo("4 ms 0 µs");
    }

    @Test public void
    should_format_nanoseconds_to_seconds_and_milliseconds() {
        assertThat(formatter.format(new ExecutionTime(3_099_999_000L, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 s 100 ms");
    }

    @Test public void
    should_format_nanoseconds_to_seconds_and_zero_millisecond() {
        assertThat(formatter.format(new ExecutionTime(3_999_999_000L, TimeUnit.NANOSECONDS)))
                .isEqualTo("4 s 0 ms");
    }

    @Test public void
    should_format_nanoseconds_to_minutes_and_seconds_and_milliseconds_and_append_nanoseconds() {
        long nanoSeconds = (long) ( ((3 * 60 + 18) * Math.pow(10, 9)) + (5 * Math.pow(10, 6)) );
        assertThat(formatter.format(new ExecutionTime(nanoSeconds, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 m 18 s 5 ms");
    }

    @Test public void
    should_format_nanoseconds_to_hours_minutes_and_seconds() {
        long nanoSeconds = (long) ((((24 + 4) * 60 * 60) + (3 * 60 + 18)) * Math.pow(10, 9));
        assertThat(formatter.format(new ExecutionTime(nanoSeconds, TimeUnit.NANOSECONDS)))
                .isEqualTo("28 h 3 m 18 s");
    }

    @Test public void
    should_format_micro_seconds() {
        assertThat(formatter.format(new ExecutionTime(12, TimeUnit.MICROSECONDS)))
                .isEqualTo("12 µs 0 ns");
    }

    @Test public void
    should_format_to_milli_seconds() {
        assertThat(formatter.format(new ExecutionTime(12, TimeUnit.MILLISECONDS)))
                .isEqualTo("12 ms 0 µs");
    }

    @Test public void
    should_format_seconds() {
        assertThat(formatter.format(new ExecutionTime(12, TimeUnit.SECONDS)))
                .isEqualTo("12 s 0 ms");
    }

    @Test public void
    should_format_to_hours() {
        assertThat(formatter.format(new ExecutionTime(12, TimeUnit.HOURS)))
                .isEqualTo("12 h 0 m 0 s");
    }

    @Test
    public void should_format_days_to_hours() {
        assertThat(formatter.format(new ExecutionTime(1, TimeUnit.DAYS)))
                .isEqualTo("24 h 0 m 0 s");
    }

    @Test public void
    should_format_nanoseconds_to_micro_seconds_and_nanoseconds() {
        assertThat(formatter.format(new ExecutionTime((3 * 1000) + 12, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 µs 12 ns");
    }

    @Test public void
    should_format_microseconds_to_milli_seconds_and_micro_seconds() {
        assertThat(formatter.format(new ExecutionTime((3 * 1000) + 12, TimeUnit.MICROSECONDS)))
                .isEqualTo("3 ms 12 µs");
    }

    @Test public void
    should_format_milliseconds_to_seconds_and_milli_seconds() {
        assertThat(formatter.format(new ExecutionTime((3 * 1000) + 12, TimeUnit.MILLISECONDS)))
                .isEqualTo("3 s 12 ms");
    }

    @Test public void
    should_format_seconds_to_minutes_and_seconds() {
        assertThat(formatter.format(new ExecutionTime((3 * 60) + 12, TimeUnit.SECONDS)))
                .isEqualTo("3 m 12 s 0 ms");
    }

    @Test public void
    should_format_minutes_to_hours_minutes_and_zero_second() {
        assertThat(formatter.format(new ExecutionTime((3 * 60) + 12, TimeUnit.MINUTES)))
                .isEqualTo("3 h 12 m 0 s");
    }

    @Test public void
    should_format_hours_to_hours_minute_second() {
        assertThat(formatter.format(new ExecutionTime((3 * 24) + 12, TimeUnit.HOURS)))
                .isEqualTo("84 h 0 m 0 s");
    }

    @Test public void
    should_format_nanoseconds_to_minutes_with_rounding_on_milli_seconds() {
        long oneBillion = (long) Math.pow(10, 9);
        long threeMinutesInNanoSeconds = 3 * 60 * oneBillion;
        long fifteenSecondsInNanoSeconds = 15 * oneBillion;
        long fiveHundredMilliSeconds = (long) (500 * Math.pow(10, 6));
        long fiveHundredMicroSeconds = (long) (500 * Math.pow(10, 3));
        long nanoSecondsToTest =    threeMinutesInNanoSeconds + fifteenSecondsInNanoSeconds
                                  + fiveHundredMilliSeconds + fiveHundredMicroSeconds;
        assertThat(formatter.format(new ExecutionTime(nanoSecondsToTest, TimeUnit.NANOSECONDS)))
                .isEqualTo("3 m 16 s 501 ms");
    }

    @Test public void
    should_format_nanoseconds_to_hours_with_rounding_on_seconds() {
        long oneBillion = (long) Math.pow(10, 9);
        long threeDaysInNanoSeconds = 3 * 24 * 3600 * oneBillion;
        long fourHoursInNanoSeconds = 4 * 3600 * oneBillion;
        long threeMinutesInNanoSeconds = 3 * 60 * oneBillion;
        long fifteenSecondsInNanoSeconds = 15 * oneBillion;
        long milliSecondsCloseToOneSecond = (long) (999 * Math.pow(10, 6));
        long nanoSecondsToTest =  threeDaysInNanoSeconds    + fourHoursInNanoSeconds
                                + threeMinutesInNanoSeconds + fifteenSecondsInNanoSeconds
                                + milliSecondsCloseToOneSecond;
        assertThat(formatter.format(new ExecutionTime(nanoSecondsToTest, TimeUnit.NANOSECONDS)))
                .isEqualTo("76 h 3 m 16 s");
    }

    @Test public void
    should_format_nanoseconds_to_hours_with_rounding_on_minutes() {
        long oneBillion = (long) Math.pow(10, 9);
        long threeDaysInNanoSeconds = 3 * 24 * 3600 * oneBillion;
        long fourHoursInNanoSeconds = 4 * 3600 * oneBillion;
        long threeMinutesInNanoSeconds = 3 * 60 * oneBillion;
        long secondsInNanoSeconds = 59 * oneBillion;
        long milliSecondsCloseToOneSecond = (long) (999 * Math.pow(10, 6));
        long nanoSecondsToTest =  threeDaysInNanoSeconds    + fourHoursInNanoSeconds
                                + threeMinutesInNanoSeconds + secondsInNanoSeconds
                                + milliSecondsCloseToOneSecond;
        assertThat(formatter.format(new ExecutionTime(nanoSecondsToTest, TimeUnit.NANOSECONDS)))
                .isEqualTo("76 h 4 m 0 s");
    }

    @Test public void
    should_format_nanoseconds_to_hours_with_rounding_on_hours() {
        long oneBillion = (long) Math.pow(10, 9);
        long fourHoursInNanoSeconds = 4 * 3600 * oneBillion;
        long minutesInNanoSeconds = 59 * 60 * oneBillion;
        long secondsInNanoSeconds = 59 * oneBillion;
        long milliSecondsCloseToOneSecond = (long) (999 * Math.pow(10, 6));
        long nanoSecondsToTest =   fourHoursInNanoSeconds + minutesInNanoSeconds
                                 + secondsInNanoSeconds   + milliSecondsCloseToOneSecond;
        assertThat(formatter.format(new ExecutionTime(nanoSecondsToTest, TimeUnit.NANOSECONDS)))
                .isEqualTo("5 h 0 m 0 s");
    }

}