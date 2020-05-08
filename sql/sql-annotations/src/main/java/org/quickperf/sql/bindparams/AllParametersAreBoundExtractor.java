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

package org.quickperf.sql.bindparams;

import net.ttddyy.dsproxy.QueryInfo;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

public class AllParametersAreBoundExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final AllParametersAreBoundExtractor INSTANCE = new AllParametersAreBoundExtractor();

    private AllParametersAreBoundExtractor() {}

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if(oneUnbindParameter(query)) {
                    return BooleanMeasure.FALSE;
                }
            }
        }
        return BooleanMeasure.TRUE;
    }

    private boolean oneUnbindParameter(QueryInfo query) {
        final String queryString = query.getQuery();

        String queryStrippedOfQuotes = stripQuotesContent(queryString);

        final String queryInLowerCase = queryStrippedOfQuotes.toLowerCase();

        if(queryInLowerCase.contains("where")){
            String[] splitWhere = queryInLowerCase.split("where");
            String[] andOrParts = splitWhere[1].split("and | or");
            for (String wherePart : andOrParts) {
                wherePart = wherePart.replaceAll(" ", "");
                if (!wherePart.contains("=?")) {
                    return true;
                }
            }
        }
        return false;
    }

    private String stripQuotesContent(final String queryString) {
        String[] queryElements = queryString.split("");
        String queryStrippedOfQuotes = "";
        boolean isBetweenQuotes = false;
        for (String queryElement : queryElements) {
            if (queryElement.equals("'")) {
                isBetweenQuotes = !isBetweenQuotes;
            } else if (!isBetweenQuotes) {
                queryStrippedOfQuotes += queryElement;
            }
        }
        return queryStrippedOfQuotes;
    }

}
