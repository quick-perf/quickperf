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

package org.quickperf.jvm.jmc.value.allocationrate;

import org.openjdk.jmc.common.item.*;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.unit.QuantityConversionException;
import org.openjdk.jmc.common.unit.UnitLookup;
import org.openjdk.jmc.flightrecorder.JfrAttributes;
import org.openjdk.jmc.flightrecorder.jdk.JdkAggregators;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;

public class AllocationRateRetriever {

    public static final AllocationRateRetriever INSTANCE = new AllocationRateRetriever();

    private AllocationRateRetriever() { }

    public AllocationRate retrieveAllocationRateFrom(IItemCollection jfrEvents) {

        long allocationDurationInMs;
        try {
            allocationDurationInMs = computeAllocationDurationInMs(jfrEvents);
        } catch (QuantityConversionException e) {
            return AllocationRate.NONE;
        }

        if (allocationDurationInMs == 0) {
            return AllocationRate.NONE;
        }

        long totalAllocationInBytes = computeTotalAllocationInBytes(jfrEvents);

        return new AllocationRate(totalAllocationInBytes, allocationDurationInMs);
    }


    private long computeTotalAllocationInBytes(IItemCollection jfrEvents) {
        IQuantity totalAlloc = jfrEvents.getAggregate(JdkAggregators.ALLOCATION_TOTAL);
        return totalAlloc.longValue();
    }

    private long computeAllocationDurationInMs(IItemCollection jfrEvents) throws QuantityConversionException {

        IItemCollection insideTlab = jfrEvents.apply(JdkFilters.ALLOC_INSIDE_TLAB);
        IItemCollection outsideTlab = jfrEvents.apply(JdkFilters.ALLOC_OUTSIDE_TLAB);

        return    searchMaxTimeStampInMs(insideTlab, outsideTlab)
                - searchMinTimeStampInMs(insideTlab, outsideTlab);

    }

    private long searchMaxTimeStampInMs(IItemCollection insideTlab, IItemCollection outsideTlab) throws QuantityConversionException {
        long insideTlabMaxTimeStamp = computeMaxTimeStampInMs(insideTlab);
        long outsideTlabMaxTimeStamp = computeMaxTimeStampInMs(outsideTlab);
        return Math.max(insideTlabMaxTimeStamp, outsideTlabMaxTimeStamp);
    }

    private long searchMinTimeStampInMs(IItemCollection insideTlab, IItemCollection outsideTlab) throws QuantityConversionException {
        long insideTlabMinTimeStamp = computeMinTimeStampInMs(insideTlab);
        long outsideTlabMinTimeStamp = computeMinTimeStampInMs(outsideTlab);
        return Math.min(insideTlabMinTimeStamp, outsideTlabMinTimeStamp);
    }

    private long computeMinTimeStampInMs(IItemCollection allocationEvents)
            throws ArithmeticException, QuantityConversionException {
        long minTimeStamp = Long.MAX_VALUE;
        for (IItemIterable jfrEventCollection : allocationEvents) {
            for (IItem item : jfrEventCollection) {
                long currentTimeStamp = getTimeStampInMs(item);
                minTimeStamp = Math.min(minTimeStamp, currentTimeStamp);
            }
        }
        return minTimeStamp;
    }

    private long computeMaxTimeStampInMs(IItemCollection allocationEvents)
            throws ArithmeticException, QuantityConversionException {
        long maxTimeStamp = 0;
        for (IItemIterable jfrEventCollection : allocationEvents) {
            for (IItem item : jfrEventCollection) {
                long currentTimeStamp = getTimeStampInMs(item);
                maxTimeStamp = Math.max(maxTimeStamp, currentTimeStamp);
            }
        }
        return maxTimeStamp;
    }

    @SuppressWarnings("unchecked")
    private long getTimeStampInMs(IItem allocationEvent) throws ArithmeticException, QuantityConversionException {

        IType<IItem> type = (IType<IItem>) allocationEvent.getType();

        IMemberAccessor<IQuantity, IItem> endTimeAccessor = JfrAttributes.END_TIME.getAccessor(type);
        IQuantity quantityEndTime = endTimeAccessor.getMember(allocationEvent);

        return quantityEndTime.longValueIn(UnitLookup.EPOCH_MS);

    }

}
