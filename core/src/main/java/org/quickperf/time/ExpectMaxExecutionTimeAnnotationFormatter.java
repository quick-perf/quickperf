/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.time;

import org.quickperf.annotation.ExpectMaxExecutionTime;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

class ExpectMaxExecutionTimeAnnotationFormatter {

    static final ExpectMaxExecutionTimeAnnotationFormatter INSTANCE = new ExpectMaxExecutionTimeAnnotationFormatter();

    private ExpectMaxExecutionTimeAnnotationFormatter() {}

    String format(ExpectMaxExecutionTime annotation) {

        SortedSet<TimeUnit> unitsToDisplay = findUnitsToDisplay(annotation);

        TreeMap<TimeUnit, Long> timeValueByTimeUnit = buildTimeValueByUnit(annotation
                                                                         , unitsToDisplay);

        return toString(timeValueByTimeUnit);

    }

    private SortedSet<TimeUnit> findUnitsToDisplay(ExpectMaxExecutionTime annotation) {

        TimeUnit minUnitToDisplay = findMinUnitToDisplay(annotation);
        TimeUnit maxUnitToDisplay = findMaxUnitToDisplay(annotation);

        TreeSet<TimeUnit> timeUnits = new TreeSet<>();
        timeUnits.add(TimeUnit.HOURS);
        timeUnits.add(TimeUnit.MINUTES);
        timeUnits.add(TimeUnit.SECONDS);
        timeUnits.add(TimeUnit.MILLISECONDS);
        timeUnits.add(TimeUnit.MICROSECONDS);
        timeUnits.add(TimeUnit.NANOSECONDS);

        return timeUnits.subSet( minUnitToDisplay, true
                               , maxUnitToDisplay, true);
    }

    private TreeMap<TimeUnit, Long> buildTimeValueByUnit(ExpectMaxExecutionTime annotation, SortedSet<TimeUnit> unitsToDisplay) {
        TreeMap<TimeUnit, Long> valuesByTimeUnit = new TreeMap<>();
        for (TimeUnit unitToDisplay : unitsToDisplay) {
            if(unitToDisplay.equals(TimeUnit.HOURS)) {
                valuesByTimeUnit.put(TimeUnit.HOURS, (long) annotation.hours());
            } else if(unitToDisplay.equals(TimeUnit.MINUTES)) {
                valuesByTimeUnit.put(TimeUnit.MINUTES, (long) annotation.minutes());
            } else if(unitToDisplay.equals(TimeUnit.SECONDS)) {
                valuesByTimeUnit.put(TimeUnit.SECONDS, (long) annotation.seconds());
            } else if(unitToDisplay.equals(TimeUnit.MILLISECONDS)) {
                valuesByTimeUnit.put(TimeUnit.MILLISECONDS, (long) annotation.milliSeconds());
            }
        }
        return valuesByTimeUnit;
    }

    private String toString(TreeMap<TimeUnit, Long> valuesByTimeUnit) {

        StringBuilder resultAsStringBuilder = new StringBuilder();

        int insertNumber = 0;
        Set<TimeUnit> timeUnits = valuesByTimeUnit.keySet();

        for (TimeUnit unitToDisplay : timeUnits) {
            insertNumber++;
            boolean lastInsert =  timeUnits.size() == insertNumber;
            String headValue = lastInsert ? "" : " ";
            String value = headValue + valuesByTimeUnit.get(unitToDisplay);
            resultAsStringBuilder.insert(0,
                    value + " " + getUnitSymbol(unitToDisplay));
        }

        return resultAsStringBuilder.toString();

    }

    private TimeUnit findMinUnitToDisplay(ExpectMaxExecutionTime annotation) {
        if(annotation.milliSeconds() != 0) {
            return TimeUnit.MILLISECONDS;
        }
        if(annotation.seconds() != 0) {
            return TimeUnit.SECONDS;
        }
        if(annotation.minutes() != 0) {
            return TimeUnit.MINUTES;
        }
        if(annotation.hours() != 0) {
            return TimeUnit.HOURS;
        }
        return TimeUnit.NANOSECONDS;
    }

    private TimeUnit findMaxUnitToDisplay(ExpectMaxExecutionTime annotation) {
        if(annotation.hours() != 0) {
            return TimeUnit.HOURS;
        }
        if(annotation.minutes() != 0) {
            return TimeUnit.MINUTES;
        }
        if(annotation.seconds() != 0) {
            return TimeUnit.SECONDS;
        }
        if(annotation.milliSeconds() != 0) {
            return TimeUnit.MILLISECONDS;
        }
        return TimeUnit.HOURS;
    }

    private String getUnitSymbol(TimeUnit timeUnit) {
        if (TimeUnit.HOURS.equals(timeUnit)) {
            return ExecutionTimeFormatter.HOUR;
        }
        if (TimeUnit.MINUTES.equals(timeUnit)) {
            return ExecutionTimeFormatter.MINUTE;
        }
        if (TimeUnit.SECONDS.equals(timeUnit)) {
            return ExecutionTimeFormatter.SECOND;
        }
        if (TimeUnit.MILLISECONDS.equals(timeUnit)) {
            return ExecutionTimeFormatter.MILLI_SECOND;
        }
        if (TimeUnit.MICROSECONDS.equals(timeUnit)) {
            return ExecutionTimeFormatter.MICRO_SECOND;
        }
        return ExecutionTimeFormatter.NANO_SECOND;
    }

}
