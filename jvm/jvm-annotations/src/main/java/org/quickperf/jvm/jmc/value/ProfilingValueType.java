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
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.flightrecorder.jdk.JdkAggregators;

import java.util.ArrayList;
import java.util.Collection;

public enum ProfilingValueType {

    ALL {

        @Override
        public IAggregator getAggregator() {
            return null;
        }

        @Override
        public Class<?> getType() {
            return null;
        }

    }
    ,
    TOTAL_GC_PAUSE {

        public IAggregator getAggregator() {
            return JdkAggregators.TOTAL_GC_PAUSE;
        }

        public Class<?> getType() {
            return IQuantity.class;
        }

     }
     ,
    LONGEST_GC_PAUSE {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.LONGEST_GC_PAUSE;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

    }
    ,
    ALLOCATION_TOTAL {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.ALLOCATION_TOTAL;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

    }
    ,
    ALLOC_INSIDE_TLAB_SUM {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.ALLOC_INSIDE_TLAB_SUM;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

    }
    ,
    ALLOC_OUTSIDE_TLAB_SUM {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.ALLOC_OUTSIDE_TLAB_SUM;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }
    }
    ,
    EXCEPTIONS_COUNT {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.EXCEPTIONS_COUNT;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

    }
    ,
    ERROR_COUNT {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.ERROR_COUNT;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

        }
    ,
    THROWABLES_COUNT {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.THROWABLES_COUNT;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }
    }
    ,
    COMPILATIONS_COUNT {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.COMPILATIONS_COUNT;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;

        }
    }
    ,
    LONGEST_COMPILATION {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.LONGEST_COMPILATION;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

        @Override
        String getLabel() {
            return "Longest compilation";
        }

    }
    ,
    CODE_CACHE_FULL_COUNT {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.CODE_CACHE_FULL_COUNT;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

    },
    JVM_NAME {

        public IAggregator getAggregator() {
            return JdkAggregators.JVM_NAME;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

    }
    ,
    JVM_VERSION {

        public IAggregator getAggregator() {
            return JdkAggregators.JVM_VERSION;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

    }
    ,
    JVM_ARGUMENTS {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.JVM_ARGUMENTS;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        }
    ,
    MIN_HW_THREADS {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.MIN_HW_THREADS;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

        }
    ,
    MIN_NUMBER_OF_CORES {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.MIN_NUMBER_OF_CORES;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

        }
    ,
    MIN_NUMBER_OF_SOCKETS {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.MIN_NUMBER_OF_SOCKETS;
        }

        @Override
        public Class<?> getType() {
            return IQuantity.class;
        }

        }
    ,
    CPU_DESCRIPTION {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.CPU_DESCRIPTION;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

    }
    ,
    OS_VERSION {

        @Override
        public IAggregator getAggregator() {
            return JdkAggregators.OS_VERSION;
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

    }
    ;

    public abstract IAggregator getAggregator();

    public abstract Class<?> getType();

    String getLabel() {

        if(getType().isAssignableFrom(IQuantity.class)) {
            IAggregator quantityAggregator = getAggregator();
            return getLabelFrom(quantityAggregator);
        }

        if(getType().isAssignableFrom(String.class)) {
            return getAggregator().getName();
        }

        throw new IllegalStateException(getType() + " type is not managed.");

    }

    private String getLabelFrom(IAggregator quantityAggregator) {
        String label = quantityAggregator.getDescription();
        if(label == null) {
            label = getAggregator().getName();
        }
        return label;
    }

    public String formatAggregate(Object aggregateAsObject) {

        if(getType().isAssignableFrom(IQuantity.class)) {
            IQuantity quantityAggregate = (IQuantity) aggregateAsObject;
            return formatIQuantityType(quantityAggregate);
        }

        if(getType().isAssignableFrom(String.class)) {
            String stringValue = (String) aggregateAsObject;
            return formatStringType(stringValue);
        }

        throw new IllegalStateException(getType() + " type is not managed.");

    }

    private String formatIQuantityType(IQuantity quantity) {
        return getLabel() + ": " + getDescriptionOf(quantity);
    }

    private String getDescriptionOf(IQuantity quantity) {
        if (quantity != null) {
            return quantity.displayUsing(IDisplayable.AUTO);
        }
        return "0";
    }

    private String formatStringType(String stringValue) {

        String[] descriptionLines = splitInLines(stringValue);

        boolean oneLine = descriptionLines.length == 1;

        return    getLabel()
                + (oneLine ? ": " : "")
                + formatDescription(stringValue);

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

        for (String descriptionLine : descriptionLines) {
            formattedDescription.append(System.lineSeparator());
            formattedDescription.append("\t" + "\t" + descriptionLine);
        }

        return formattedDescription.toString();

    }

    public static ProfilingValueType[] getValuesWithoutAll() {
        Collection<ProfilingValueType> profilingValueTypesWithoutAll
                = new ArrayList<>(ProfilingValueType.values().length - 1);
        for (ProfilingValueType profilingValueType : ProfilingValueType.values()) {
            if (profilingValueType != ProfilingValueType.ALL) {
                profilingValueTypesWithoutAll.add(profilingValueType);
            }
        }
        return profilingValueTypesWithoutAll.toArray(new ProfilingValueType[0]);
    }

}
