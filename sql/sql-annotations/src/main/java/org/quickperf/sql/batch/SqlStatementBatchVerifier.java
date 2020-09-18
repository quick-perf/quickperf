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

package org.quickperf.sql.batch;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.sql.annotation.ExpectJdbcBatching;
import org.quickperf.sql.framework.JdbcSuggestion;

public class SqlStatementBatchVerifier implements VerifiablePerformanceIssue<ExpectJdbcBatching, SqlBatchSizes> {

    public static final SqlStatementBatchVerifier INSTANCE = new SqlStatementBatchVerifier();

    private SqlStatementBatchVerifier() {}

    @Override
    public PerfIssue verifyPerfIssue(ExpectJdbcBatching annotation, SqlBatchSizes measuredSqlBatchSizes) {

        int expectedBatchSize = annotation.batchSize();

        int[] measuredBatchSizesAsArray = measuredSqlBatchSizes.getValue();

        boolean userHasGivenBatchSize = expectedBatchSize != -1;
        if (userHasGivenBatchSize) {
            return verifyBatchSize(expectedBatchSize
                                 , measuredBatchSizesAsArray);
        }

        return verifyThatInsertUpdateDeleteExecutionAreBatched(measuredBatchSizesAsArray);

    }

    private PerfIssue verifyThatInsertUpdateDeleteExecutionAreBatched(int[] measuredBatchSizesAsArray) {
        for (int measuredBatchSize : measuredBatchSizesAsArray) {
            if (measuredBatchSize == 0) {
                String description = "JDBC batching is disabled."
                                   + System.lineSeparator()
                                   + JdbcSuggestion.BATCHING.getMessage()
                                   + System.lineSeparator()
                                   + "";
                return new PerfIssue(description);
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
