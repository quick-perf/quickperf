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

package org.quickperf.sql.like;

import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

public class ContainsLikeWithLeadingWildcardExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final ContainsLikeWithLeadingWildcardExtractor INSTANCE =
            new ContainsLikeWithLeadingWildcardExtractor();

    private ContainsLikeWithLeadingWildcardExtractor() { }

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (searchLikeWithLeadingWildcardOn(query)) {
                    return BooleanMeasure.TRUE;
                }
            }
        }
        return BooleanMeasure.FALSE;
    }

    private boolean searchLikeWithLeadingWildcardOn(QueryInfo queryInfo) {
        String query = queryInfo.getQuery();
        String queryInLowerCase = query.toLowerCase();
        String queryInLowerCaseWithoutWhiteSpaces = queryInLowerCase.replace(" ", "");
        return     queryInLowerCaseWithoutWhiteSpaces.contains("like'%")
                || queryInLowerCaseWithoutWhiteSpaces.contains("like'_")
                ;
    }

}
