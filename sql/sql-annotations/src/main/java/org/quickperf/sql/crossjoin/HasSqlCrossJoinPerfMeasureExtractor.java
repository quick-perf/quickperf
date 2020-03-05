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

package org.quickperf.sql.crossjoin;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutionPredicate;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.SqlQueryPredicate;

public class HasSqlCrossJoinPerfMeasureExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final HasSqlCrossJoinPerfMeasureExtractor INSTANCE = new HasSqlCrossJoinPerfMeasureExtractor();

    private HasSqlCrossJoinPerfMeasureExtractor() { }

    private static final SqlQueryPredicate CROSS_JOIN_QUERY_PREDICATE = new SqlQueryPredicate() {
        @Override
        public boolean test(String sqlQuery) {
            String sqlQueryAsString = sqlQuery.toLowerCase();
            return sqlQueryAsString.contains("cross join");
        }
    };

    private static final SqlExecutionPredicate CROSS_JOIN_PREDICATE = new SqlExecutionPredicate() {
        @Override
        public boolean test(SqlExecution sqlExecution) {
            return sqlExecution.hasQueryFollowing(CROSS_JOIN_QUERY_PREDICATE);
        }
    };

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        boolean existsCrossJoin = sqlExecutions.oneExecutionHasQueryRespecting(CROSS_JOIN_PREDICATE);
        return new BooleanMeasure(existsCrossJoin);
    }

}
