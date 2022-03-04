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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

class ExecutionTimeFormatter {

    static final String HOUR   = "h";
    static final String MINUTE = "m";
    static final String SECOND = "s";
    static final String MILLI_SECOND = "ms";
    private static final String MICRO_SYMBOL = "\u00B5";
    static final String MICRO_SECOND = MICRO_SYMBOL + "s";
    static final String NANO_SECOND = "ns";

    static final ExecutionTimeFormatter INSTANCE = new ExecutionTimeFormatter();

    private ExecutionTimeFormatter() { }

    String formatAndAppendNanoSeconds(ExecutionTime executionTime) {

        long valueInNanoseconds = computeValueInNanoseconds(executionTime);

        if (isNanoSecondOrder(valueInNanoseconds)) {
            return valueInNanoseconds + " " + NANO_SECOND;
        }

        DecimalFormat nanoSecondFormat = buildFormatWithSpaceGroupingSeparator();
        String nanoSecondsPart = " " + "(" + nanoSecondFormat.format(valueInNanoseconds)
                                     + " " + NANO_SECOND + ")";

        return format(executionTime) + nanoSecondsPart;

    }

    private DecimalFormat buildFormatWithSpaceGroupingSeparator() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat.setDecimalFormatSymbols(symbols);
        return decimalFormat;
    }

    String format(ExecutionTime executionTime) {
        long valueInNanoseconds = computeValueInNanoseconds(executionTime);
        if (isNanoSecondOrder(valueInNanoseconds)) {
            return valueInNanoseconds + " " + NANO_SECOND;
        }
        if (isMicroSecondOrder(valueInNanoseconds)) {
            return formatWithMicroSecondsAndNanoSeconds(valueInNanoseconds);
        }
        if (isMilliSecondOrder(valueInNanoseconds)) {
            return formatWithMillisecondsAndMicroSeconds(valueInNanoseconds);
        }
        return formatWithSecondsMinutesOrHours(valueInNanoseconds);
    }

    private long computeValueInNanoseconds(ExecutionTime executionTime) {
        long value = executionTime.getValue();
        TimeUnit unit = executionTime.getUnit();
        return unit.toNanos(value);
    }

    private boolean isNanoSecondOrder(long valueInNanoseconds) {
        return valueInNanoseconds < Math.pow(10, 3);
    }

    private boolean isMicroSecondOrder(long valueInNanoseconds) {
        return valueInNanoseconds < Math.pow(10, 6);
    }

    private String formatWithMicroSecondsAndNanoSeconds(long valueInNanoseconds) {
        long nanoseconds = valueInNanoseconds % 1_000;
        long microSeconds = (valueInNanoseconds - nanoseconds) / 1_000;
        return microSeconds + " " + MICRO_SECOND + " " + nanoseconds + " " + NANO_SECOND;
    }

    private boolean isMilliSecondOrder(long valueInNanoseconds) {
        return valueInNanoseconds < Math.pow(10, 9);
    }

    private String formatWithMillisecondsAndMicroSeconds(long valueInNanoseconds) {
        long us = (valueInNanoseconds / 1_000) % 1_000;
        long ms = (((valueInNanoseconds / 1_000)) - us) / 1_000;
        long roundedUs = Math.round((valueInNanoseconds - (ms * Math.pow(10, 6))) / 1_000);
        long roundedMs = ms;
        if (roundedUs == 1_000) {
            roundedMs += 1;
            roundedUs = 0;
        }
        return roundedMs + " " + MILLI_SECOND + " " + roundedUs + " " + MICRO_SECOND;
    }

    private String formatWithSecondsMinutesOrHours(long valueInNanoseconds) {

        SecondsMilliSeconds secondsMilliSeconds = SecondsMilliSeconds.fromNanoSeconds(valueInNanoseconds);
        if (secondsMilliSeconds.isLessThanOneMinute()) {
            return    secondsMilliSeconds.getRoundedSeconds() + " " + SECOND + " "
                    + secondsMilliSeconds.getRoundedMilliseconds() + " " + MILLI_SECOND;
        }

        long secondsToReturn = Math.round(valueInNanoseconds * Math.pow(10, -9) % 60);

        long totalSeconds = Math.round(valueInNanoseconds * Math.pow(10, -9));
        long minutes = (totalSeconds - secondsToReturn) / 60;

        if (minutes < 60) {
            return minutes + " " + MINUTE
                    + " " + secondsToReturn + " " + SECOND
                    + " " + secondsMilliSeconds.getRoundedMilliseconds() + " " + MILLI_SECOND;
        }

        long minutesToReturn = minutes % 60;
        long hours = (totalSeconds - (minutesToReturn * 60) - secondsToReturn) / 3600;

        if(secondsToReturn == 60) {
            secondsToReturn = 0;
            minutesToReturn += 1;
        }

        if(minutesToReturn == 60) {
            minutesToReturn = 0;
            hours +=1;
        }

        return          hours           + " " + HOUR
                + " " + minutesToReturn + " " + MINUTE
                + " " + secondsToReturn + " " + SECOND;

    }

    private static class SecondsMilliSeconds {

        private final long valueInNanoseconds;
        private final long seconds;

        private SecondsMilliSeconds(long valueInNanoseconds, long seconds) {
            this.valueInNanoseconds = valueInNanoseconds;
            this.seconds = seconds;
        }

        static SecondsMilliSeconds fromNanoSeconds(long valueInNanoseconds) {
            long oneMillion = (long) Math.pow(10, 6);
            long ms = (valueInNanoseconds / oneMillion) % 1_000;
            long s = (valueInNanoseconds / oneMillion - ms) / 1_000;
            return new SecondsMilliSeconds(valueInNanoseconds, s);
        }

        long getRoundedMilliseconds() {
            long roundedMilliseconds = computeRoundedMilliseconds();
            if (roundedMilliseconds == 1000) {
                return 0;
            }
            return roundedMilliseconds;
        }

        private long computeRoundedMilliseconds() {
            return Math.round((this.valueInNanoseconds - (seconds * Math.pow(10, 9))) / 1_000_000);
        }

        boolean isLessThanOneMinute() {
            return getRoundedSeconds() < 60;
        }

        long getRoundedSeconds() {
            if (computeRoundedMilliseconds() == 1000) {
                return seconds + 1;
            }
            return seconds;
        }

    }

}