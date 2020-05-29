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

package org.quickperf.jvm.allocation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ByteAllocationMeasureFormatter {

    public static final ByteAllocationMeasureFormatter INSTANCE = new ByteAllocationMeasureFormatter();

    private ByteAllocationMeasureFormatter() {}

    public String formatWithAllocationInBytes(Allocation allocation) {

        if(isByteOrderOfMagnitude(allocation)) {
            return formatAllocationInBytes(allocation);
        }
        if(isKiloByteOrderOfMagnitude(allocation)) {
            return    formatAllocationInKiloBytes(allocation)
                    + formatInByteAllocationBetweenParentheses(allocation);
        }
        if(isMegaByteOrderOfMagnitude(allocation)) {
            return    formatAllocationInMegaBytes(allocation)
                    + formatInByteAllocationBetweenParentheses(allocation);
        }

        return    formatAllocationInGigaBytes(allocation)
                + formatInByteAllocationBetweenParentheses(allocation);

    }

    public String shortFormat(double allocationInBytes) {

        if(allocationInBytes < 1024) {
            return "" + allocationInBytes + " " + AllocationUnit.BYTE.shortFormat();
        }
        if(allocationInBytes < 1024 * 1024) {
            double kiloByteValue = allocationInBytes / 1024;
            String formattedAllocationValue = formatAllocationValue(kiloByteValue);
            return formattedAllocationValue + " "  + AllocationUnit.KILO_BYTE.shortFormat();
        }
        if(allocationInBytes < Math.pow(1024, 3)) {
            double megaByteValue = allocationInBytes / Math.pow(1024, 2);
            String formattedAllocationValue = formatAllocationValue(megaByteValue);
            return formattedAllocationValue + " " + AllocationUnit.MEGA_BYTE.shortFormat();
        }

        double gigaByteValue = allocationInBytes / Math.pow(1024, 3);
        String formattedAllocationValue = formatAllocationValue(gigaByteValue);
        return formattedAllocationValue + " " + AllocationUnit.GIGA_BYTE.shortFormat();

    }

    private boolean isByteOrderOfMagnitude(Allocation allocation) {
        return allocation.getValue() < 1024;
    }

    private String formatAllocationInBytes(Allocation allocation) {
        return "" + allocation.getValue() + " " + allocation.getUnit();
    }

    private boolean isKiloByteOrderOfMagnitude(Allocation allocation) {
        return allocation.getValue() < 1024 * 1024;
    }

    private String formatAllocationInKiloBytes(Allocation allocation) {

        double kiloByteValue = allocation.getValue() / 1024;

        String formattedAllocationValue = formatAllocationValue(kiloByteValue);

        return formattedAllocationValue + " "  + AllocationUnit.KILO_BYTE;

    }

    private boolean isMegaByteOrderOfMagnitude(Allocation allocation) {
        return allocation.getValue() < Math.pow(1024, 3);
    }

    private String formatAllocationInMegaBytes(Allocation allocation) {

        double megaByteValue = allocation.getValue() / Math.pow(1024, 2);

        String formattedAllocationValue = formatAllocationValue(megaByteValue);

        return formattedAllocationValue + " " + AllocationUnit.MEGA_BYTE;

    }

    private String formatAllocationInGigaBytes(Allocation allocation) {

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

    private String formatInByteAllocationBetweenParentheses(Allocation allocationValue) {
        DecimalFormat decimalFormat = buildBytePrefixFormatter();
        return " (" + decimalFormat.format(allocationValue.getValue()) + " bytes)";
    }

    private DecimalFormat buildBytePrefixFormatter() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat.setDecimalFormatSymbols(symbols);
        return decimalFormat;
    }

}