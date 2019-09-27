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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.jvm.allocation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class ByteAllocationMeasureFormatter {

    public static final ByteAllocationMeasureFormatter INSTANCE = new ByteAllocationMeasureFormatter();

    private final static DecimalFormat BYTE_PREFIX_FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);

    static {
        final DecimalFormatSymbols symbols = BYTE_PREFIX_FORMATTER.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        BYTE_PREFIX_FORMATTER.setDecimalFormatSymbols(symbols);
    }

    private ByteAllocationMeasureFormatter() {}

    public String format(Allocation allocation) {
        Double allocationValue = allocation.getValue();

        if(isByteOrderOfMagnitude(allocationValue)) {
            return formatByteAllocation(allocation);
        }

        String formatted = null;
        if(isKiloByteOrderOfMagnitude(allocationValue)) {
            formatted = formatKiloByteAllocation(allocation);
        } else if(isMegaByteOrderOfMagnitude(allocationValue)) {
            formatted = formatMegaByteAllocation(allocation);
        } else {
            formatted =  formatGigaByteAllocation(allocation);
        }

        return formatByteSuffixAllocationValue(formatted, allocation);

    }

    private boolean isByteOrderOfMagnitude(Double allocationValue) {
        return allocationValue < 1024;
    }

    private String formatByteAllocation(Allocation allocation) {
        return "" + allocation.getValue() + " " + allocation.getUnit();
    }

    private boolean isKiloByteOrderOfMagnitude(Double allocationValue) {
        return allocationValue < 1024 * 1024;
    }

    private String formatKiloByteAllocation(Allocation allocation) {

        double kiloByteValue = allocation.getValue() / 1024;

        String formattedAllocationValue = formatAllocationValue(kiloByteValue);

        return formattedAllocationValue + " "  + AllocationUnit.KILO_BYTE;

    }

    private boolean isMegaByteOrderOfMagnitude(Double allocationValue) {
        return allocationValue < Math.pow(1024, 3);
    }

    private String formatMegaByteAllocation(Allocation allocation) {

        double megaByteValue = allocation.getValue() / Math.pow(1024, 2);

        String formattedAllocationValue = formatAllocationValue(megaByteValue);

        return formattedAllocationValue + " " + AllocationUnit.MEGA_BYTE;

    }

    private String formatGigaByteAllocation(Allocation allocation) {

        double gigaByteValue = allocation.getValue() / Math.pow(1024, 3);

        String formattedAllocationValue = formatAllocationValue(gigaByteValue);

        return formattedAllocationValue + " " + AllocationUnit.GIGA_BYTE;

    }

    private String formatAllocationValue(double allocationValue) {

        String allocationValueAsString = "" + allocationValue;

        String[] splittedAllocationValue = allocationValueAsString.split("\\.");

        String integerPartAsString = splittedAllocationValue[0];

        String decimalPartAsString = splittedAllocationValue[1];

        String truncatedDecimalPartAsString = decimalPartAsString.substring(0, 1);

        return integerPartAsString + "." + truncatedDecimalPartAsString;

    }

    private String formatByteSuffixAllocationValue(String prefix, Allocation allocationValue)
    {
        return prefix + " (" + BYTE_PREFIX_FORMATTER.format(allocationValue.getValue()) +  " bytes)";
    }

}
