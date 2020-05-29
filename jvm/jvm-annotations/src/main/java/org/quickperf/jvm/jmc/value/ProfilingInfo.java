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

import org.openjdk.jmc.common.IDisplayable;
import org.openjdk.jmc.common.item.IAggregator;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.flightrecorder.jdk.JdkAggregators;
import org.quickperf.jvm.jmc.value.allocationrate.AllocationRate;
import org.quickperf.jvm.jmc.value.allocationrate.AllocationRateFormatter;
import org.quickperf.jvm.jmc.value.allocationrate.AllocationRateRetriever;

public enum ProfilingInfo {

    TOTAL_GC_PAUSE {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.TOTAL_GC_PAUSE, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.TOTAL_GC_PAUSE, IQuantity.class);
        }

    }
    ,
    LONGEST_GC_PAUSE {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.LONGEST_GC_PAUSE, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.LONGEST_GC_PAUSE, IQuantity.class);
        }

    }
    ,
    ALLOCATION_TOTAL {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.ALLOCATION_TOTAL, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.ALLOCATION_TOTAL, IQuantity.class);
        }

    }
    ,
    ALLOC_INSIDE_TLAB_SUM {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.ALLOC_INSIDE_TLAB_SUM, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.ALLOC_INSIDE_TLAB_SUM, IQuantity.class);
        }

    }
    ,
    ALLOC_OUTSIDE_TLAB_SUM {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.ALLOC_OUTSIDE_TLAB_SUM, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.ALLOC_OUTSIDE_TLAB_SUM, IQuantity.class);
        }

    }
    ,
    EXCEPTIONS_COUNT {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.EXCEPTIONS_COUNT, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.EXCEPTIONS_COUNT, IQuantity.class);
        }

    }
    ,
    ERROR_COUNT {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.ERROR_COUNT, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.ERROR_COUNT, IQuantity.class);
        }

    }
    ,
    THROWABLES_COUNT {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.THROWABLES_COUNT, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.THROWABLES_COUNT, IQuantity.class);
        }

    }
    ,
    COMPILATIONS_COUNT {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.COMPILATIONS_COUNT, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.COMPILATIONS_COUNT, IQuantity.class);
        }

    }
    ,
    LONGEST_COMPILATION {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.LONGEST_COMPILATION, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.LONGEST_COMPILATION, IQuantity.class);
        }

    }
    ,
    CODE_CACHE_FULL_COUNT {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.CODE_CACHE_FULL_COUNT, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.CODE_CACHE_FULL_COUNT, IQuantity.class);
        }

    },
    JVM_NAME {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.JVM_NAME, String.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.JVM_NAME, String.class);
        }

    }
    ,
    JVM_VERSION {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.JVM_VERSION, String.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.JVM_VERSION, String.class);
        }

    }
    ,
    JVM_ARGUMENTS {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.JVM_ARGUMENTS, String.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.JVM_ARGUMENTS, String.class);
        }

    }
    ,
    MIN_HW_THREADS {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.MIN_HW_THREADS, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.MIN_HW_THREADS, IQuantity.class);
        }

    }
    ,
    MIN_NUMBER_OF_CORES {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.MIN_NUMBER_OF_CORES, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.MIN_NUMBER_OF_CORES, IQuantity.class);
        }

    }
    ,
    MIN_NUMBER_OF_SOCKETS {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.MIN_NUMBER_OF_SOCKETS, IQuantity.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.MIN_NUMBER_OF_SOCKETS, IQuantity.class);
        }

    }
    ,
    CPU_DESCRIPTION {

        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.CPU_DESCRIPTION, String.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.CPU_DESCRIPTION, String.class);
        }

    }
    ,
    OS_VERSION {
        @Override
        public String formatAsString(IItemCollection jfrEvents) {
            return formatAsString(jfrEvents, JdkAggregators.OS_VERSION, String.class);
        }

        @Override
        public String getLabel() {
            return getLabel(JdkAggregators.OS_VERSION, String.class);
        }

    },
    ALLOCATION_RATE {
        @Override
        public String formatAsString(IItemCollection jfrEvents) {


            AllocationRate allocationRate = AllocationRateRetriever.INSTANCE
                                           .retrieveAllocationRateFrom(jfrEvents);

            return AllocationRateFormatter.INSTANCE.format(allocationRate);

        }

        @Override
        public String getLabel() {
            return "Allocation Rate";
        }
    };

    public abstract String formatAsString(IItemCollection jfrEvents);

    public abstract String getLabel();

    @SuppressWarnings("unchecked")
    String getLabel(IAggregator aggregator, Class<?> type) {

        if (type.isAssignableFrom(IQuantity.class)) {
            return getLabelFrom(aggregator);
        }

        if(type.isAssignableFrom(String.class)) {
            return aggregator.getName();
        }

        throw new IllegalStateException(type+ " type is not managed.");

    }

    @SuppressWarnings("unchecked")
    private String getLabelFrom(IAggregator aggregator) {
        String label = aggregator.getDescription();
        if(label == null) {
            label = aggregator.getName();
        }
        return label;
    }

    @SuppressWarnings("unchecked")
    public String formatAsString(IItemCollection jfrEvents, IAggregator aggregator, Class<?> type) {
        Object aggregateAsObject = jfrEvents.getAggregate(aggregator);

        if(type.isAssignableFrom(IQuantity.class)) {
            IQuantity quantity= (IQuantity) aggregateAsObject;
            return getStringValueOf(quantity);
        }

        if(type.isAssignableFrom(String.class)) {
            String stringValue = (String) aggregateAsObject;

            String[] descriptionLines = splitInLines(stringValue);

            boolean oneLine = descriptionLines.length == 1;

            return   (oneLine ? ": " : "")
                    + formatDescription(stringValue);

        }

        throw new IllegalStateException(type + " type is not managed.");

    }

    private String getStringValueOf(IQuantity quantity) {
        if (quantity != null) {
            return quantity.displayUsing(IDisplayable.AUTO);
        }
        return "0";
    }

    private String[] splitInLines(String stringValue) {
        return stringValue.split("\\r|\\n");
    }

    private String formatDescription(String description) {

        String[] descriptionLines = splitInLines(description);

        boolean oneLine = descriptionLines.length == 1;
        if (oneLine) {
            return description;
        }

        return formatDescOfSeveralLines(descriptionLines);

    }

    private String formatDescOfSeveralLines(String[] descriptionLines) {

        StringBuilder formattedDescription = new StringBuilder();

        for (int i = 0; i < descriptionLines.length; i++) {
            if (i != 0) {
                formattedDescription.append(System.lineSeparator());
            }
            String descriptionLine = descriptionLines[i];
            formattedDescription.append("\t" + "\t" + descriptionLine);
        }

        return formattedDescription.toString();

    }

}
