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

import org.quickperf.measure.AbstractComparablePerfMeasure;

public class Allocation extends AbstractComparablePerfMeasure<Allocation> {

    public static final Allocation ZERO = Allocation.ofBytes(0);

    private static final String NO_COMMENT = "";

    private final Double value;

    private final AllocationUnit unit;

    private final String comment;

    public Allocation(Double value, AllocationUnit unit) {
        this.value = value;
        this.unit = unit;
        this.comment = NO_COMMENT;
    }

    public static Allocation ofBytes(long numberOfBytes) {
        return new Allocation(Double.valueOf(numberOfBytes), AllocationUnit.BYTE);
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public AllocationUnit getUnit() {
        return unit;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(Allocation otherAllocation) {
        long valueInBytes = getValueInBytes();
        long otherValueInBytes = otherAllocation.getValueInBytes();
        return Long.compare(valueInBytes, otherValueInBytes);
    }

    public long getValueInBytes() {
        return (long) (value * unit.getValueInBytes());
    }

}
