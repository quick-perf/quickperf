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
package org.quickperf.sql.batch;

import org.quickperf.issue.PerfIssue;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.perfrecording.SelfVerifiablePerfRecord;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.unit.CountUnit;

import java.io.Serializable;

/*
* Different batch sizes of batch executions.
*/
class SqlBatchSizes implements PerfMeasure<int[], CountUnit>, SelfVerifiablePerfRecord, Serializable {

    private static final String NO_COMMENT = "";

    private final int[] measuredBatchSizes;

    /** True when the batch executions were recorded while SQL of a worker
     * thread could not be attributed to a single test. */
    private volatile boolean crossTestContamination;

    SqlBatchSizes(int[] measuredBatchSizes) {
        this.measuredBatchSizes = measuredBatchSizes;
    }

    @Override
    public int[] getValue() {
        return measuredBatchSizes;
    }

    @Override
    public CountUnit getUnit() {
        return CountUnit.COUNT;
    }

    @Override
    public String getComment() {
        return NO_COMMENT;
    }

    void markCrossTestContamination() {
        crossTestContamination = true;
    }

    /** Batch sizes recorded under cross-test contamination may include
     * executions of a sibling test: the test fails with
     * {@link SqlExecutions#CROSS_TEST_CONTAMINATION_MESSAGE}, even if its
     * JDBC batching is respected. */
    @Override
    public PerfIssue verifyRecord() {
        if (crossTestContamination) {
            return new PerfIssue(SqlExecutions.CROSS_TEST_CONTAMINATION_MESSAGE);
        }
        return PerfIssue.NONE;
    }

}
