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
package org.quickperf.sql.execution;

import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.SqlExecutions;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.sql.select.analysis.SelectAnalysisExtractor;
import org.quickperf.unit.Count;

public class SqlAnalysisExtractor implements ExtractablePerformanceMeasure<SqlExecutions, SqlAnalysis> {

    public static final SqlAnalysisExtractor INSTANCE = new SqlAnalysisExtractor();

    private SqlAnalysisExtractor() { }

    @Override
    public SqlAnalysis extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        SelectAnalysis selectAnalysis = SelectAnalysisExtractor.INSTANCE.extractPerfMeasureFrom(sqlExecutions);
        Count queriesSendingNumber = new Count(sqlExecutions.getNumberOfExecutions());
        return new SqlAnalysis(queriesSendingNumber, selectAnalysis, sqlExecutions);
    }

}
