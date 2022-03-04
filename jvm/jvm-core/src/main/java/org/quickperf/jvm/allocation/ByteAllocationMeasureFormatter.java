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
package org.quickperf.jvm.allocation;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class ByteAllocationMeasureFormatter {

    public static final ByteAllocationMeasureFormatter INSTANCE = new ByteAllocationMeasureFormatter();

    private ByteAllocationMeasureFormatter() {}

    public String format(Allocation allocation) {
        long valueInBytes = allocation.getValueInBytes();
        return format(valueInBytes, false);
    }

    public String formatAndAppendAllocationInBytes(Allocation allocation) {

        long valueInBytes = allocation.getValueInBytes();

        String formattedValue = format(valueInBytes, false);

        if (isBytesOrder(valueInBytes)) {
            return formattedValue;
        }

        return formattedValue + formatInByteAllocationBetweenParentheses(allocation);

    }

    public String shortFormat(long allocationInBytes, int numberOfDigits) {
        return format(allocationInBytes, numberOfDigits, true);
    }

    private String format(long allocationInBytes, boolean isShortFormat) {
        int numberOfDigits = 2;
        return format(allocationInBytes, numberOfDigits, isShortFormat);
    }

    private String format(long allocationInBytes, int numberOfDigits, boolean isShortFormat) {
        AllocationUnit allocationUnit = findMaxAllocationUnit(allocationInBytes);
        String allocationUnitAsString = isShortFormat ? allocationUnit.toShortString()
                                                      : allocationUnit.toString();
        return    reduceAllocationValue(allocationInBytes, numberOfDigits)
                + " " + allocationUnitAsString;
    }

    private String reduceAllocationValue(long allocationInBytes, int numberOfDigits) {

        if(isBytesOrder(allocationInBytes)) {
            int noDigit = 0;
            return formatAllocationValue(allocationInBytes, noDigit);
        }
        if(allocationInBytes < 1024 * 1024) {
            double kiloByteValue = (double)(allocationInBytes) / 1024;
            return formatAllocationValue(kiloByteValue, numberOfDigits);
        }
        if(allocationInBytes < Math.pow(1024, 3)) {
            double megaByteValue = allocationInBytes / Math.pow(1024, 2);
            return formatAllocationValue(megaByteValue, numberOfDigits);
        }

        double gigaByteValue = allocationInBytes / Math.pow(1024, 3);
        return formatAllocationValue(gigaByteValue, numberOfDigits);
    }

    private AllocationUnit findMaxAllocationUnit(double allocationInBytes) {
        if(isBytesOrder(allocationInBytes)) {
            return AllocationUnit.BYTE;
        }
        if(allocationInBytes < 1024 * 1024) {
            return AllocationUnit.KILO_BYTE;
        }
        if(allocationInBytes < Math.pow(1024, 3)) {
            return AllocationUnit.MEGA_BYTE;
        }
        return AllocationUnit.GIGA_BYTE;
    }

    private boolean isBytesOrder(double allocationInBytes) {
        return allocationInBytes < 1024;
    }

    private String formatAllocationValue(double allocationValue, int numberOfDigits) {
        NumberFormat numberFormat = DecimalFormat.getInstance(Locale.US);
        numberFormat.setMinimumFractionDigits(numberOfDigits);
        numberFormat.setMaximumFractionDigits(numberOfDigits);
        numberFormat.setGroupingUsed(false);
        return numberFormat.format(allocationValue);
    }

    private String formatInByteAllocationBetweenParentheses(Allocation allocationValue) {
        DecimalFormat format = buildAllocationValueInBytesFormat();
        return " (" + format.format(allocationValue.getValueInBytes()) + " " + AllocationUnit.BYTE +  ")";
    }

    private DecimalFormat buildAllocationValueInBytesFormat() {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        decimalFormat.setDecimalFormatSymbols(symbols);
        return decimalFormat;
    }

}