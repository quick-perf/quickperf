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

package org.quickperf.jvm.jmc.value;

import org.openjdk.jmc.common.item.IItemCollection;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.ProfileJvm;

import static org.quickperf.jvm.jmc.value.ProfilingInfo.*;

public class DisplayJvmProfilingValueVerifier implements
    VerifiablePerformanceIssue<ProfileJvm, JfrEventsMeasure> {

    public static DisplayJvmProfilingValueVerifier INSTANCE = new DisplayJvmProfilingValueVerifier();

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String LINE = "------------------------------------------------------------------------------" + LINE_SEPARATOR;


    private DisplayJvmProfilingValueVerifier() {
    }

    @Override
    public PerfIssue verifyPerfIssue(ProfileJvm annotation, JfrEventsMeasure jfrEventsMeasure) {

        IItemCollection jfrEvents = jfrEventsMeasure.getValue();

        System.out.println();

        String allocationTotal = ALLOCATION_TOTAL.formatAsString(jfrEvents);
        String insideTlabSum = ALLOC_INSIDE_TLAB_SUM.formatAsString(jfrEvents);
        String outsideTlabSum = ALLOC_OUTSIDE_TLAB_SUM.formatAsString(jfrEvents);
        String allocationRate = ALLOCATION_RATE.formatAsString(jfrEvents);

        String totalGcPause = TOTAL_GC_PAUSE.formatAsString(jfrEvents);
        String gcPause = LONGEST_GC_PAUSE.formatAsString(jfrEvents);

        String exceptionsCount = EXCEPTIONS_COUNT.formatAsString(jfrEvents);

        String longestCompilation = LONGEST_COMPILATION.formatAsString(jfrEvents);

        String errorCount = ERROR_COUNT.formatAsString(jfrEvents);
        String throwablesCount = THROWABLES_COUNT.formatAsString(jfrEvents);

        String compilationsCount = COMPILATIONS_COUNT.formatAsString(jfrEvents);
        String codeCacheFullCount = CODE_CACHE_FULL_COUNT.getLabel() + ": " + CODE_CACHE_FULL_COUNT.formatAsString(jfrEvents);

        String jvmName = JVM_NAME.formatAsString(jfrEvents);
        String jvmVersion = JVM_VERSION.formatAsString(jfrEvents);
        String jvmArguments = JVM_ARGUMENTS.formatAsString(jfrEvents);

        String minHwThreads = MIN_HW_THREADS.formatAsString(jfrEvents);
        String minNumberOfCores = MIN_NUMBER_OF_CORES.formatAsString(jfrEvents);
        String minNumberOfSockets = MIN_NUMBER_OF_SOCKETS.formatAsString(jfrEvents);
        String cpuDescription = CPU_DESCRIPTION.formatAsString(jfrEvents);

        String osVersion = OS_VERSION.formatAsString(jfrEvents);

        StringWidthAdapter twelveLength = new StringWidthAdapter(12);

        StringWidthAdapter fifteenLength = new StringWidthAdapter(15);

        StringWidthAdapter twentyNineLength = new StringWidthAdapter(29);

        StringWidthAdapter thirtyLength = new StringWidthAdapter(30);

        String text =
            LINE
            + " ALLOCATION (estimations)"                          + "     |   " + "GARBAGE COLLECTION           "                               + "|  THROWABLE" + LINE_SEPARATOR
            + " Total       : " + fifteenLength.adapt(allocationTotal)  + "|   " + twentyNineLength.adapt("Total pause: " + totalGcPause) + "|  Exception: " + exceptionsCount + LINE_SEPARATOR
            + " Inside TLAB : " + fifteenLength.adapt(insideTlabSum)    + "|   " + twentyNineLength.adapt("Longest GC pause: " + gcPause) + "|  Error: " + errorCount + LINE_SEPARATOR
            + " Outside TLAB: " + fifteenLength.adapt(outsideTlabSum)   + "|   " + twentyNineLength.adapt("")                             + "|  Throwable: " + throwablesCount + LINE_SEPARATOR
            + " Allocation rate: " + twelveLength.adapt(allocationRate) + "|   " + twentyNineLength.adapt("")                             + "|" + LINE_SEPARATOR
            + LINE
            + thirtyLength.adapt(" COMPILATION")                    + "|   " + "CODE CACHE" + LINE_SEPARATOR
            + thirtyLength.adapt(" Number: " + compilationsCount)   + "|   " + codeCacheFullCount + LINE_SEPARATOR
            + thirtyLength.adapt(" Longest: " + longestCompilation) + "|   " + LINE_SEPARATOR
            + LINE
            + " " + "JVM" + LINE_SEPARATOR
            + " Name: " + jvmName + LINE_SEPARATOR
            + " Version: " + jvmVersion + LINE_SEPARATOR
            + " Arguments: " + jvmArguments + LINE_SEPARATOR
            + LINE
            + " " + "HARDWARE" + LINE_SEPARATOR
            + " Hardware threads: " + minHwThreads + LINE_SEPARATOR
            + " Cores: " + minNumberOfCores + LINE_SEPARATOR
            + " Sockets: " + minNumberOfSockets + LINE_SEPARATOR
            + " CPU: " + LINE_SEPARATOR
            + cpuDescription + LINE_SEPARATOR
            + LINE
            + " OS:" + LINE_SEPARATOR
            + osVersion + LINE_SEPARATOR
            + LINE;

        System.out.println(text);

        return PerfIssue.NONE;

    }

}
