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

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;

public class MaxHeapAllocationPerfVerifier implements VerifiablePerformanceIssue<ExpectMaxHeapAllocation, Allocation> {

    public static final VerifiablePerformanceIssue INSTANCE = new MaxHeapAllocationPerfVerifier();

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;

    private MaxHeapAllocationPerfVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxHeapAllocation annotation, Allocation measuredAllocation) {

        Allocation maxExpectedAllocation = new Allocation(annotation.value(), annotation.unit());

        if(maxExpectedAllocation.isLessThan(measuredAllocation)) {

            String assertionMessage =
                      "Expected allocation (test method thread) to be less than "
                     + byteAllocationMeasureFormatter.formatWithAllocationInBytes(maxExpectedAllocation)
                     + " but is " + byteAllocationMeasureFormatter.formatWithAllocationInBytes(measuredAllocation) + ".";
            String description = assertionMessage + System.lineSeparator() + measuredAllocation.getComment();

            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }

}
