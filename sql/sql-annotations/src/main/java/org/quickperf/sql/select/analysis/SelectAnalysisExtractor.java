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
package org.quickperf.sql.select.analysis;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.sql.QueryTypeRetriever;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectAnalysisExtractor implements ExtractablePerformanceMeasure<SqlExecutions, SelectAnalysis> {

    public static final SelectAnalysisExtractor INSTANCE = new SelectAnalysisExtractor();

    private SelectAnalysisExtractor() { }

    @Override
    public SelectAnalysis extractPerfMeasureFrom(SqlExecutions sqlExecutions) {

        int selectNumber = sqlExecutions.retrieveQueryNumberOfType(QueryType.SELECT);

        boolean sameSelectTypesWithDifferentParamValues = false;

        int sameSelectsNumber = 0;

        SqlSelects sqlSelects = new SqlSelects();
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (isSelectType(query)) {
                    if (!sameSelectTypesWithDifferentParamValues
                     && sqlSelects.sameSqlQueryWithDifferentParams(query)) {
                        sameSelectTypesWithDifferentParamValues = true;
                    }
                    if( sqlSelects.exactlySameSqlQueryExists(query)) {
                        if(sameSelectsNumber == 0) {
                            sameSelectsNumber = 1;
                        }
                        sameSelectsNumber++;
                    }
                    sqlSelects.add(query);
                }

            }
        }

        return new SelectAnalysis(selectNumber
                                , sameSelectsNumber
                                , sameSelectTypesWithDifferentParamValues
        );

    }

    private boolean isSelectType(QueryInfo query) {
        QueryType queryType = QueryTypeRetriever.INSTANCE.typeOf(query);
        return QueryType.SELECT.equals(queryType);
    }

    private static class SqlSelects {

        private final Map<String, ParamsCalls> callsParamsByQuery = new HashMap<>();

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

        boolean sameSqlQueryWithDifferentParams(QueryInfo query) {
            String queryAsString = query.getQuery();
            ParamsCalls paramsCalls = callsParamsByQuery.get(queryAsString);
            if (paramsCalls == null) {
                return false;
            }
            List<Object> paramsList = QueryParamsExtractor.INSTANCE.getParamsOf(query);
            return !paramsCalls.alreadySameParamsCalled(paramsList);
        }

        boolean exactlySameSqlQueryExists(QueryInfo query) {
            String queryAsString = query.getQuery();
            ParamsCalls paramsCalls = callsParamsByQuery.get(queryAsString);
            if (paramsCalls == null) {
                return false;
            }
            List<Object> paramsList = QueryParamsExtractor.INSTANCE.getParamsOf(query);
            return paramsCalls.alreadySameParamsCalled(paramsList);
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
