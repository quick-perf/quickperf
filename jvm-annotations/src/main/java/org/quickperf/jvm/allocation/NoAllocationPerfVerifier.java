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

import org.quickperf.PerfIssue;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.ExpectNoAllocation;

public class NoAllocationPerfVerifier implements VerifiablePerformanceIssue<ExpectNoAllocation, Allocation> {

    private static final Allocation ZERO_ALLOCATION = new Allocation(0D, AllocationUnit.BYTE);

    public static final NoAllocationPerfVerifier INSTANCE = new NoAllocationPerfVerifier();

    private NoAllocationPerfVerifier() {}

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;


    @Override
    public PerfIssue verifyPerfIssue(ExpectNoAllocation annotation, Allocation measuredAllocation) {

        if(!ZERO_ALLOCATION.isEqualTo(measuredAllocation)) {
            String assertionMessage =
                    "Expected allocation to be 0 but is " + byteAllocationMeasureFormatter.format(measuredAllocation) + ".";
            String description = assertionMessage + System.lineSeparator() + measuredAllocation.getComment();
            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }
}
