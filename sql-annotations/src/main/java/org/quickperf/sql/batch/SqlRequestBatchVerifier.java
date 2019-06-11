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

package org.quickperf.sql.batch;

import org.quickperf.PerfIssue;
import org.quickperf.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectJdbcBatching;

public class SqlRequestBatchVerifier implements VerifiablePerformanceIssue<ExpectJdbcBatching, SqlBatchSizes> {

    public static final SqlRequestBatchVerifier INSTANCE = new SqlRequestBatchVerifier();

    private SqlRequestBatchVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectJdbcBatching annotation, SqlBatchSizes measuredSqlBatchSizes) {

        int expectedBatchSize = annotation.batchSize();

        int[] measuredBatchSizesAsArray = measuredSqlBatchSizes.getValue();

        boolean userHasGivenBatchSize = expectedBatchSize != -1;
        if (!userHasGivenBatchSize) {
            return verifyThatInsertUpdateDeleteExecutionAreBatched(measuredBatchSizesAsArray);
        }

        return verifyBatchSize(expectedBatchSize
                             , measuredBatchSizesAsArray);

    }

    private PerfIssue verifyThatInsertUpdateDeleteExecutionAreBatched(int[] measuredBatchSizesAsArray) {
        for (int measuredBatchSize : measuredBatchSizesAsArray) {
            if (measuredBatchSize == 0) {
                return new PerfIssue("SQL executions were supposed to be batched.");
            }
        }
        return PerfIssue.NONE;
    }

    private int findNumberOfBatchSizeToCheck(int[] measuredBatchSizesAsArray) {
        if(measuredBatchSizesAsArray.length == 1) {
            return 1;
        }
        return measuredBatchSizesAsArray.length - 1;
    }

    private PerfIssue verifyBatchSize(int expectedBatchSize, int[] measuredBatchSizesAsArray) {
        int numberOfBatchSizeToCheck = findNumberOfBatchSizeToCheck(measuredBatchSizesAsArray);

        for (int i = 0; i < numberOfBatchSizeToCheck; i++) {
            int measuredBatchSize = measuredBatchSizesAsArray[i];
            if (measuredBatchSize != expectedBatchSize) {
                return buildNotRespectedBatchSizePerfIssue(expectedBatchSize, measuredBatchSize);
            }

        }
        return PerfIssue.NONE;
    }

    private PerfIssue buildNotRespectedBatchSizePerfIssue(int expectedBatchSize, int measuredBatchSize) {
        String description
            = "Expected batch size <" + expectedBatchSize
            + "> but is <" + measuredBatchSize + ">.";
        return new PerfIssue(description);
    }

}
