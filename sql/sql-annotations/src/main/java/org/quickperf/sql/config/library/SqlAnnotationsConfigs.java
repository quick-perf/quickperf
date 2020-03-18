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

import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.sql.PersistenceSqlRecorder;
import org.quickperf.sql.annotation.*;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;
import org.quickperf.sql.batch.SqlStatementBatchVerifier;
import org.quickperf.sql.crossjoin.HasSqlCrossJoinPerfMeasureExtractor;
import org.quickperf.sql.crossjoin.NoSqlCrossJoinPerfIssueVerifier;
import org.quickperf.sql.delete.DeleteCountMeasureExtractor;
import org.quickperf.sql.delete.NumberOfSqlDeletePerfIssueVerifier;
import org.quickperf.sql.display.DisplaySqlOfTestMethodBodyRecorder;
import org.quickperf.sql.display.DisplaySqlRecorder;
import org.quickperf.sql.insert.InsertCountMeasureExtractor;
import org.quickperf.sql.insert.InsertNumberPerfIssueVerifier;
import org.quickperf.sql.like.ContainsLikeWithLeadingWildcardExtractor;
import org.quickperf.sql.like.HasLikeWithLeadingWildcardVerifier;
import org.quickperf.sql.select.*;
import org.quickperf.sql.select.columns.MaxSelectedColumnsPerMeasureExtractor;
import org.quickperf.sql.select.columns.MaxSelectedColumnsPerfIssueVerifier;
import org.quickperf.sql.select.columns.SelectedColumnNumberPerfIssueVerifier;
import org.quickperf.sql.select.columns.SelectedColumnNumberPerfMeasureExtractor;
import org.quickperf.sql.time.SqlQueryExecutionTimeExtractor;
import org.quickperf.sql.time.SqlQueryMaxExecutionTimeVerifier;
import org.quickperf.sql.update.UpdateCountMeasureExtractor;
import org.quickperf.sql.update.UpdateNumberPerfIssueVerifier;
import org.quickperf.sql.update.columns.MaxUpdatedColumnsPerMeasureExtractor;
import org.quickperf.sql.update.columns.MaxUpdatedColumnsPerfIssueVerifier;
import org.quickperf.sql.update.columns.UpdatedColumnsMeasureExtractor;
import org.quickperf.sql.update.columns.UpdatedColumnsPerfIssueVerifier;

class SqlAnnotationsConfigs {

    private SqlAnnotationsConfigs() { }

	static final AnnotationConfig DISABLE_EXACTLY_SAME_SQL_SELECTS = new AnnotationConfig.Builder()
			.perfRecorderClass(PersistenceSqlRecorder.class)
			.perfMeasureExtractor(HasExactlySameSelectExtractor.INSTANCE)
			.perfIssueVerifier(HasExactlySameSelectVerifier.INSTANCE)
			.build(DisableExactlySameSelects.class);

	static final AnnotationConfig ENABLE_EXACTLY_SAME_SQL_SELECTS = new AnnotationConfig.Builder()
			.cancelBehaviorOf(DisableExactlySameSelects.class)
			.build(EnableExactlySameSelects.class);

    static final AnnotationConfig NUMBER_OF_SQL_SELECT = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(SelectCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(SelectNumberPerfIssueVerifier.INSTANCE)
            .build(ExpectSelect.class);

    static final AnnotationConfig MAX_SQL_SELECT = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(SelectCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(MaxOfSelectsPerfIssueVerifier.INSTANCE)
            .build(ExpectMaxSelect.class);

    static final AnnotationConfig DISPLAY_ALL_SQL = new AnnotationConfig.Builder()
            .perfRecorderClass(DisplaySqlRecorder.class)
            .build(DisplaySql.class);

    static final AnnotationConfig DISPLAY_SQL = new AnnotationConfig.Builder()
            .perfRecorderClass(DisplaySqlOfTestMethodBodyRecorder.class)
            .build(DisplaySqlOfTestMethodBody.class);

	static final AnnotationConfig DISABLE_SAME_SELECT_TYPES_WITH_DIFFERENT_PARAMS = new AnnotationConfig.Builder()
			.perfRecorderClass(PersistenceSqlRecorder.class)
			.perfMeasureExtractor(HasSameSelectTypesWithDiffParamsExtractor.INSTANCE)
			.perfIssueVerifier(HasSameSelectTypesWithDiffParamValuesVerifier.INSTANCE)
			.build(DisableSameSelectTypesWithDifferentParamValues.class);

	static final AnnotationConfig ENABLE_SAME_SELECT_TYPES_WITH_DIFFERENT_PARAMS = new AnnotationConfig.Builder()
			.cancelBehaviorOf(DisableSameSelectTypesWithDifferentParamValues.class)
			.build(EnableSameSelectTypesWithDifferentParamValues.class);

    static final AnnotationConfig NUMBER_OF_SQL_INSERT = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(InsertCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(InsertNumberPerfIssueVerifier.INSTANCE)
            .build(ExpectInsert.class);

    static final AnnotationConfig SQL_STATEMENTS_BATCHED = new AnnotationConfig.Builder()
            .perfRecorderClass(SqlStatementBatchRecorder.class)
            .perfIssueVerifier(SqlStatementBatchVerifier.INSTANCE)
            .build(ExpectJdbcBatching.class);

    static final AnnotationConfig NUMBER_OF_SQL_DELETE = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(DeleteCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(NumberOfSqlDeletePerfIssueVerifier.INSTANCE)
            .build(ExpectDelete.class);

    static final AnnotationConfig NUMBER_OF_SQL_UPDATE = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(UpdateCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(UpdateNumberPerfIssueVerifier.INSTANCE)
            .build(ExpectUpdate.class);

    static final AnnotationConfig MAX_SELECTED_COLUMNS = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(MaxSelectedColumnsPerMeasureExtractor.INSTANCE)
            .perfIssueVerifier(MaxSelectedColumnsPerfIssueVerifier.INSTANCE)
            .build(ExpectMaxSelectedColumn.class);

	static final AnnotationConfig MAX_UPDATED_COLUMNS = new AnnotationConfig.Builder()
			.perfRecorderClass(PersistenceSqlRecorder.class)
			.perfMeasureExtractor(MaxUpdatedColumnsPerMeasureExtractor.INSTANCE)
			.perfIssueVerifier(MaxUpdatedColumnsPerfIssueVerifier.INSTANCE)
			.build(ExpectMaxUpdatedColumn.class);

    static final AnnotationConfig NUMBER_OF_SELECTED_COLUMNS = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(SelectedColumnNumberPerfMeasureExtractor.INSTANCE)
            .perfIssueVerifier(SelectedColumnNumberPerfIssueVerifier.INSTANCE)
            .build(ExpectSelectedColumn.class);

    static final AnnotationConfig DISABLE_SQL_CROSS_JOIN = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(HasSqlCrossJoinPerfMeasureExtractor.INSTANCE)
            .perfIssueVerifier(NoSqlCrossJoinPerfIssueVerifier.INSTANCE)
            .build(DisableCrossJoin.class);

    static final AnnotationConfig ENABLE_SQL_CROSS_JOIN = new AnnotationConfig.Builder()
            .cancelBehaviorOf(DisableCrossJoin.class)
            .build(EnableCrossJoin.class);

    static final AnnotationConfig DISABLE_LIKE_STARTING_WITH_WILDCARD = new AnnotationConfig.Builder()
            .perfRecorderClass(PersistenceSqlRecorder.class)
            .perfMeasureExtractor(ContainsLikeWithLeadingWildcardExtractor.INSTANCE)
            .perfIssueVerifier(HasLikeWithLeadingWildcardVerifier.INSTANCE)
            .build(DisableLikeWithLeadingWildcard.class);

    static final AnnotationConfig ENABLE_LIKE_STARTING_WITH_WILDCARD = new AnnotationConfig.Builder()
            .cancelBehaviorOf(DisableLikeWithLeadingWildcard.class)
            .build(EnableLikeWithLeadingWildcard.class);
    
    static final AnnotationConfig EXPECT_MAX_QUERY_EXECUTION_TIME = new AnnotationConfig.Builder()
			.perfRecorderClass(PersistenceSqlRecorder.class)
			.perfMeasureExtractor(SqlQueryExecutionTimeExtractor.INSTANCE)
			.perfIssueVerifier(SqlQueryMaxExecutionTimeVerifier.INSTANCE)
			.build(ExpectMaxQueryExecutionTime.class);
    
    static final AnnotationConfig EXPECT_UPDATED_COLUMN = new AnnotationConfig.Builder()
    		.perfRecorderClass(PersistenceSqlRecorder.class)
    		.perfMeasureExtractor(UpdatedColumnsMeasureExtractor.INSTANCE)
    		.perfIssueVerifier(UpdatedColumnsPerfIssueVerifier.INSTANCE)
    		.build(ExpectUpdatedColumn.class);

}
