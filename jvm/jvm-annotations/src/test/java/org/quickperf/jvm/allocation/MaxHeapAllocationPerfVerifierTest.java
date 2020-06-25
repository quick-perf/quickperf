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

import org.junit.Test;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.JvmAnnotationBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxHeapAllocationPerfVerifierTest {

    @Test
    public void should_return_a_perf_issue_if_heap_allocation_is_greater_than_expected() {

        // GIVEN
        ExpectMaxHeapAllocation annotation = JvmAnnotationBuilder.expectMaxHeapAllocation(3.75 * 1024
                                                                                        , AllocationUnit.MEGA_BYTE);

        // 3.75 giga bytes
        Allocation measuredAllocation = Allocation.ofBytes(4724464025L);

        // WHEN
        VerifiablePerformanceIssue maxHeapAllocationPerfIssueVerifier = MaxHeapAllocationPerfVerifier.INSTANCE;

        @SuppressWarnings("unchecked")
        PerfIssue perfIssue = maxHeapAllocationPerfIssueVerifier.verifyPerfIssue(annotation, measuredAllocation);

        // THEN
        assertThat(perfIssue).isNotEqualTo(PerfIssue.NONE);
        String description = perfIssue.getDescription();
        assertThat(description).contains("Expected heap allocation (test method thread) to be less than 3.75 Giga bytes but is 4.40 Giga bytes (4 724 464 025 bytes).");

    }

}