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

import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;

public class AllocationRateFormatter {

    public static final AllocationRateFormatter INSTANCE = new AllocationRateFormatter();

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;

    private AllocationRateFormatter() { }

    public String format(AllocationRate allocationRate) {

        if (allocationRate.isNone()) {
            return " ";
        }

        double allocationRateInBytesPerSecond = allocationRate.getValueInBytesBySecond();

        return byteAllocationMeasureFormatter.shortFormat(allocationRateInBytesPerSecond) + "/s";

    }



}
