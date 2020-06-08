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

package org.quickperf.jvm.rss;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.allocation.Allocation;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.allocation.ByteAllocationMeasureFormatter;
import org.quickperf.jvm.annotations.ExpectMaxRSS;

public class ExpectMaxRssPerfVerifier implements VerifiablePerformanceIssue<ExpectMaxRSS, ProcessStatus> {

    public static final VerifiablePerformanceIssue INSTANCE = new ExpectMaxRssPerfVerifier();

    private final ByteAllocationMeasureFormatter byteAllocationMeasureFormatter = ByteAllocationMeasureFormatter.INSTANCE;

    private ExpectMaxRssPerfVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(ExpectMaxRSS annotation, ProcessStatus processStatus) {

        Allocation maxExpectedRss = new Allocation(annotation.value(), annotation.unit());
        Allocation measuredRss = new Allocation((double) processStatus.getRssInKb() * 1024, AllocationUnit.BYTE);

        if (maxExpectedRss.isLessThan(measuredRss)) {

            String assertionMessage =
                              "Expected RSS to be less than "
                            + byteAllocationMeasureFormatter.formatWithAllocationInBytes(maxExpectedRss)
                            + " but is " + byteAllocationMeasureFormatter.formatWithAllocationInBytes(measuredRss) + ".";
            String description = assertionMessage + System.lineSeparator() + measuredRss.getComment();

            return new PerfIssue(description);
        }

        return PerfIssue.NONE;

    }
}