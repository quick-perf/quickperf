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

import org.openjdk.jmc.common.item.IAggregator;
import org.openjdk.jmc.common.item.IItemCollection;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.DisplayJvmProfilingValue;

public class DisplayJvmProfilingValueVerifier implements VerifiablePerformanceIssue<DisplayJvmProfilingValue, JfrEventsMeasure>  {

    public static DisplayJvmProfilingValueVerifier INSTANCE = new DisplayJvmProfilingValueVerifier();

    private DisplayJvmProfilingValueVerifier() { }

    @SuppressWarnings("unchecked")
    @Override
    public PerfIssue verifyPerfIssue(DisplayJvmProfilingValue annotation, JfrEventsMeasure jfrEventsMeasure) {

        IItemCollection jfrEvents = jfrEventsMeasure.getValue();

        ProfilingValueType[] profilingValueTypes = annotation.valueType();

        if (all(profilingValueTypes)) {
            profilingValueTypes = ProfilingValueType.getValuesWithoutAll();
        }

        System.out.println();
        System.out.println("[QUICK PERF] JVM profiling values");

        for (ProfilingValueType profilingValueType : profilingValueTypes) {

            IAggregator aggregator = profilingValueType.getAggregator();

            Object aggregate = jfrEvents.getAggregate(aggregator);

            System.out.println("\t * " + profilingValueType.formatAggregate(aggregate));

        }

        return PerfIssue.NONE;

    }

    private boolean all(ProfilingValueType[] profilingValueTypes) {
        for (ProfilingValueType profilingValueType : profilingValueTypes) {
            if(profilingValueType.equals(ProfilingValueType.ALL)) {
                return true;
            }
        }
        return false;
    }

}
