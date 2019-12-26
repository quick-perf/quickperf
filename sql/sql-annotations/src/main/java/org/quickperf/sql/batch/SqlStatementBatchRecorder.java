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

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;
import org.quickperf.sql.QueryTypeRetriever;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;

import java.util.List;

public class SqlStatementBatchRecorder implements SqlRecorder<SqlBatchSizes> {

    private static final String BATCH_FILE_NAME = "ExpectJdbcBatching.ser";

    private boolean previousStatementsAreBatched = true;

    /* int array is used to avoid boxing since a batch can contain a lot of
       insert, delete or update sql orders.*/
    private int[] differentBatchSizes = new int[0];

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.unregister(this);
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            saveCharacteristicsOfBatchExecutions(differentBatchSizes, workingFolder);
        }
    }

    private void saveCharacteristicsOfBatchExecutions(int[] batchExecutions, WorkingFolder workingFolder) {
        ObjectFileRepository objectFileRepository = ObjectFileRepository.INSTANCE;
        objectFileRepository.save(workingFolder, BATCH_FILE_NAME, new SqlBatchSizes(batchExecutions));
    }

    @Override
    public SqlBatchSizes findRecord(TestExecutionContext testExecutionContext) {

        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            ObjectFileRepository objectFileRepository = ObjectFileRepository.INSTANCE;
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            return (SqlBatchSizes) objectFileRepository.find(workingFolder.getPath()
                                                           , BATCH_FILE_NAME);
        }

        return new SqlBatchSizes(differentBatchSizes);
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries, int listenerIdentifier) {
        for (QueryInfo query : queries) {
            if (       previousStatementsAreBatched
                    && isRequestTypeInsertOrUpdateOrDeleteType(query)
                ) {
                int batchSize = execInfo.getBatchSize();
                if (isNewBatchSize(batchSize)) {
                    differentBatchSizes = createTableWithNewBatchSize(batchSize);
                }
                previousStatementsAreBatched = execInfo.isBatch();
            }
        }
    }

    private int[] createTableWithNewBatchSize(int batchSize) {
        int newTableLength = differentBatchSizes.length + 1;
        int[] newTable = new int[newTableLength];
        System.arraycopy(differentBatchSizes, 0, newTable
                        , 0, differentBatchSizes.length);
        newTable[newTableLength - 1] = batchSize;
        return newTable;
    }

    private boolean isNewBatchSize(int batchSize) {
        for (int currentBatchSize : differentBatchSizes) {
            if (batchSize == currentBatchSize) {
                return false;
            }
        }
        return true;
    }

    private boolean isRequestTypeInsertOrUpdateOrDeleteType(QueryInfo query) {
        QueryTypeRetriever typeRetriever = QueryTypeRetriever.INSTANCE;
        QueryType queryType = typeRetriever.typeOf(query);
        return     queryType.equals(QueryType.INSERT)
                || queryType.equals(QueryType.UPDATE)
                || queryType.equals(QueryType.DELETE);
    }

    @Override
    public void cleanResources() {}

}
