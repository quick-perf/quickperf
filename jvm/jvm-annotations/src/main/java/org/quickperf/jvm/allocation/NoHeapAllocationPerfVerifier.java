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

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.ExpectNoHeapAllocation;

public class NoHeapAllocationPerfVerifier implements VerifiablePerformanceIssue<ExpectNoHeapAllocation, Allocation> {

    public static final NoHeapAllocationPerfVerifier INSTANCE = new NoHeapAllocationPerfVerifier();

    private NoHeapAllocationPerfVerifier() {}

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;

    @Override
    public PerfIssue verifyPerfIssue(ExpectNoHeapAllocation annotation, Allocation measuredAllocation) {

        if(!Allocation.ZERO.isEqualTo(measuredAllocation)) {
            String assertionMessage =
                    "Expected no heap allocation (test method thread) but is " + byteAllocationMeasureFormatter.formatAndAppendAllocationInBytes(measuredAllocation) + ".";
            String description = assertionMessage + System.lineSeparator() + measuredAllocation.getComment();
            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }

}
