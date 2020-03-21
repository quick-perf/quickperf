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

package org.quickperf.sql.select;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.QueryTypeRetriever;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

import java.util.*;

public class HasExactlySameSelectExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final HasExactlySameSelectExtractor INSTANCE = new HasExactlySameSelectExtractor();

    private HasExactlySameSelectExtractor() {}

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        SqlSelects sqlSelects = new SqlSelects();
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (       isSelectType(query)
                        && sqlSelects.exactlySameSqlQueryExists(query)
                    ) {
                        return BooleanMeasure.TRUE;
                    }
                if (isSelectType(query)) {
                    sqlSelects.add(query);
                }
            }
        }
        return BooleanMeasure.FALSE;
    }

    private boolean isSelectType(QueryInfo query) {
        QueryType queryType = QueryTypeRetriever.INSTANCE.typeOf(query);
        return QueryType.SELECT.equals(queryType);
    }

    private static class SqlSelects {

        private final Map<String, ParamsCalls> callsParamsByQuery = new HashMap<>();

        boolean exactlySameSqlQueryExists(QueryInfo query) {
            String queryAsString = query.getQuery();
            ParamsCalls paramsCalls = callsParamsByQuery.get(queryAsString);
            if (paramsCalls == null) {
                return false;
            }
            List<Object> paramsList = QueryParamsExtractor.INSTANCE.getParamsOf(query);
            return paramsCalls.alreadySameParamsCalled(paramsList);
        }

        void add(QueryInfo query) {
            String queryAsString = query.getQuery();
            ParamsCalls paramsCalls = callsParamsByQuery.get(queryAsString);
            if (paramsCalls == null) {
                paramsCalls = new ParamsCalls();
            }
            List<Object> paramsList = QueryParamsExtractor.INSTANCE.getParamsOf(query);
            paramsCalls.addParams(paramsList);
            callsParamsByQuery.put(queryAsString, paramsCalls);
        }

        private static class ParamsCalls {

            private final List<List<Object>> paramsCalls = new ArrayList<>();

            boolean alreadySameParamsCalled(List<Object> params) {
                return paramsCalls.contains(params);
            }

            void addParams(List<Object> params) {
                paramsCalls.add(params);
            }

        }

    }

}
