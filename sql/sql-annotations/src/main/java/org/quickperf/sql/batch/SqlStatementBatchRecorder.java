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

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;
import org.quickperf.sql.QueryTypeRetriever;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SqlStatementBatchRecorder implements SqlRecorder<SqlBatchSizes> {

    private static final String BATCH_FILE_NAME = "ExpectJdbcBatching.ser";

    // Guarded by differentBatchSizes
    private boolean previousStatementsAreBatched = true;

    // LinkedHashSet preserves insertion order, which @ExpectJdbcBatching's
    // verifier depends on: verifyBatchSize() checks every batch except the
    // last (so the "first batch wins" position must be the chronologically
    // first observed batch size). A ConcurrentHashMap-backed set would
    // iterate in hash order and silently flip the check result for cases
    // like 70 inserts batched as [30, 30, 10] - hash order would yield
    // [10, 30] and verifyBatchSize would compare expected 30 to measured
    // 10. Several threads can write concurrently: the threads of the test,
    // and the worker threads taking the fallback of SqlRecorderRegistry. The
    // synchronized wrapper makes each write atomic, and visible to the
    // assertion thread that later reads through findRecord().
    private final Set<Integer> differentBatchSizes =
            Collections.synchronizedSet(new LinkedHashSet<Integer>());

    // True when an insert, update or delete was recorded while the SQL could
    // be attributed to several recording tests: the batch sizes may include
    // executions of a sibling test. A select broadcast to several tests does
    // not change the batch sizes.
    private volatile boolean crossTestContamination;

    @Override
    public void startRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.INSTANCE.register(this);
    }

    @Override
    public void stopRecording(TestExecutionContext testExecutionContext) {
        SqlRecorderRegistry.unregister(this);
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            saveCharacteristicsOfBatchExecutions(toIntArray(differentBatchSizes), workingFolder);
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

        SqlBatchSizes sqlBatchSizes = new SqlBatchSizes(toIntArray(differentBatchSizes));

        // Read after copying the batch sizes: a thread sets the flag before
        // adding its batch size, so the flag covers every copied batch size
        // attributed to several tests. Single-shot per test: findRecord
        // returns a new record on every call, so the contamination is
        // reported once.
        boolean crossTestContamination = this.crossTestContamination;
        this.crossTestContamination = false;

        if (crossTestContamination) {
            // SqlBatchSizes.verifyRecord() turns the tag into a perf issue
            // failing the test.
            sqlBatchSizes.markCrossTestContamination();
        }
        return sqlBatchSizes;
    }

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries) {
        if (isEmptyBatch(execInfo)) {
            return;
        }
        for (QueryInfo query : queries) {
            if (       isRequestTypeInsertOrUpdateOrDeleteType(query)
                    && SqlRecorderRegistry.isSqlOfCurrentThreadAttributedToSeveralTests()
                ) {
                crossTestContamination = true;
            }
            if (isRequestTypeInsertOrUpdateOrDeleteType(query)) {
                recordBatchSize(execInfo);
            }
        }
    }

    // Atomic, as several threads can record: no batch size is recorded after
    // an execution that is not batched.
    private void recordBatchSize(ExecutionInfo execInfo) {
        synchronized (differentBatchSizes) {
            if (previousStatementsAreBatched) {
                int batchSize = execInfo.getBatchSize();
                differentBatchSizes.add(batchSize);
                previousStatementsAreBatched = execInfo.isBatch();
            }
        }
    }

    private boolean isEmptyBatch(ExecutionInfo execInfo) {
        // An executeBatch() with nothing added sends no SQL, but its batch size is 0, like an unbatched execution.
        // It is ignored, so that a recorded 0 batch size always means an unbatched execution
        return execInfo.isBatch() && execInfo.getBatchSize() == 0;
    }

    private int[] toIntArray(Set<Integer> sizes) {
        // Manual synchronization because Collections.synchronizedSet documents
        // that iteration MUST be performed inside a synchronized(sizes) block
        // - the wrapper synchronizes single operations but not multi-op
        // iterators. Snapshot under the lock, then build the int[] without
        // the lock held.
        Integer[] snapshot;
        synchronized (sizes) {
            snapshot = sizes.toArray(new Integer[0]);
        }
        int[] array = new int[snapshot.length];
        for (int i = 0; i < snapshot.length; i++) {
            array[i] = snapshot[i];
        }
        return array;
    }

    private boolean isRequestTypeInsertOrUpdateOrDeleteType(QueryInfo query) {
        QueryTypeRetriever typeRetriever = QueryTypeRetriever.INSTANCE;
        QueryType queryType = typeRetriever.typeOf(query);
        return     queryType.equals(QueryType.INSERT)
                || queryType.equals(QueryType.UPDATE)
                || queryType.equals(QueryType.DELETE);
    }

    @Override
    public void cleanResources() {
        crossTestContamination = false;
        // The thread recording the SQL of a timed-out test can keep running,
        // for example in a JDBC call ignoring the interrupt, and never stop
        // recording. Without this, the test would stay a recording test, and
        // the SQL of the worker threads of the next tests would be attributed
        // to several tests. Unregistering again has no effect.
        SqlRecorderRegistry.unregister(this);
    }

}
