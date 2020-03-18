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

package org.quickperf.sql.config.library;

import org.quickperf.RecorderExecutionOrder;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigLoader;
import org.quickperf.sql.PersistenceSqlRecorder;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;
import org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.util.Arrays;
import java.util.Collection;

public class SqlConfigLoader implements QuickPerfConfigLoader {

    @Override
    public Collection<AnnotationConfig> loadAnnotationConfigs() {
        return Arrays.asList(
                  SqlAnnotationsConfigs.NUMBER_OF_SQL_SELECT
                , SqlAnnotationsConfigs.MAX_SQL_SELECT
                , SqlAnnotationsConfigs.NUMBER_OF_SQL_INSERT
                , SqlAnnotationsConfigs.NUMBER_OF_SQL_DELETE
                , SqlAnnotationsConfigs.NUMBER_OF_SQL_UPDATE
                , SqlAnnotationsConfigs.MAX_SELECTED_COLUMNS
                , SqlAnnotationsConfigs.MAX_UPDATED_COLUMNS
                , SqlAnnotationsConfigs.NUMBER_OF_SELECTED_COLUMNS
                , SqlAnnotationsConfigs.DISABLE_SQL_CROSS_JOIN
                , SqlAnnotationsConfigs.ENABLE_SQL_CROSS_JOIN
                , SqlAnnotationsConfigs.DISABLE_LIKE_STARTING_WITH_WILDCARD
                , SqlAnnotationsConfigs.ENABLE_LIKE_STARTING_WITH_WILDCARD
                , SqlAnnotationsConfigs.SQL_STATEMENTS_BATCHED
                , SqlAnnotationsConfigs.DISPLAY_ALL_SQL
                , SqlAnnotationsConfigs.DISPLAY_SQL
                , SqlAnnotationsConfigs.DISABLE_SAME_SELECT_TYPES_WITH_DIFFERENT_PARAMS
                , SqlAnnotationsConfigs.ENABLE_SAME_SELECT_TYPES_WITH_DIFFERENT_PARAMS
                , SqlAnnotationsConfigs.DISABLE_EXACTLY_SAME_SQL_SELECTS
                , SqlAnnotationsConfigs.ENABLE_EXACTLY_SAME_SQL_SELECTS
                , SqlAnnotationsConfigs.EXPECT_MAX_QUERY_EXECUTION_TIME
                , SqlAnnotationsConfigs.EXPECT_UPDATED_COLUMN
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersBeforeTestMethod() {
        return Arrays.asList(
                  new RecorderExecutionOrder(PersistenceSqlRecorder.class, 2000)
                , new RecorderExecutionOrder(DisplaySqlRecorder.class, 2001)
                , new RecorderExecutionOrder(DisplaySqlOfTestMethodBodyRecorder.class, 2002)
                , new RecorderExecutionOrder(SqlStatementBatchRecorder.class, 2003)

        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersAfterTestMethod() {
        return Arrays.asList(
        		  new RecorderExecutionOrder(PersistenceSqlRecorder.class, 7000)
                , new RecorderExecutionOrder(DisplaySqlRecorder.class, 7001)
                , new RecorderExecutionOrder(DisplaySqlOfTestMethodBodyRecorder.class, 7002)
                , new RecorderExecutionOrder(SqlStatementBatchRecorder.class, 7003)
        );
    }

}
