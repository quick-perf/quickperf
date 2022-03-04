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
package org.quickperf.sql.repository;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;
import org.quickperf.sql.SqlExecutions;

import java.io.File;
import java.util.List;

class SqlFileRepository implements SqlRepository {

    private static final String SQL_FILE_NAME = "sql.ser";

    private final SqlMemoryRepository sqlMemoryRepository = new SqlMemoryRepository();

    private boolean flushed;

    private final ObjectFileRepository objectFileRepository = ObjectFileRepository.getInstance();

    @Override
    public void addQueryExecution(ExecutionInfo execInfo, List<QueryInfo> queries) {
        sqlMemoryRepository.addQueryExecution(execInfo, queries);
    }

    @Override
    public void flush(WorkingFolder workingFolder) {
        if(!flushed) {
            SqlExecutions executedQueries = sqlMemoryRepository.findExecutedQueries(workingFolder);
            if(!executedQueries.isEmpty()) {
                objectFileRepository.save(workingFolder, SQL_FILE_NAME, executedQueries);
            }
            flushed = true;
        }
    }

    @Override
    public SqlExecutions findExecutedQueries(WorkingFolder workingFolder) {
        SqlExecutions sqlExecutionsFromMemory = sqlMemoryRepository.findExecutedQueries(workingFolder);
        if(sqlExecutionsFromMemory.isEmpty()) {
            SqlExecutions sqlExecutionsFromFile = retrieveExecutedQueriesFromFile(workingFolder);
            sqlMemoryRepository.saveSqlExecutions(sqlExecutionsFromFile);
            return sqlExecutionsFromFile;
        }
        return sqlExecutionsFromMemory;
    }

    private SqlExecutions retrieveExecutedQueriesFromFile(WorkingFolder workingFolder) {
        if(!sqlFileExists(workingFolder)) {
            return SqlExecutions.NONE;
        }
        Object sqlExecutionsAsObject = objectFileRepository.find(workingFolder.getPath(), SQL_FILE_NAME);
        if(sqlExecutionsAsObject == null) {
            return SqlExecutions.NONE;
        }
        return (SqlExecutions) sqlExecutionsAsObject;
    }

    private boolean sqlFileExists(WorkingFolder workingFolder) {
        String workingFolderPath = workingFolder.getPath();
        File sqlFile = new File(workingFolderPath + File.separator + SQL_FILE_NAME);
        return sqlFile.exists();
    }

}
