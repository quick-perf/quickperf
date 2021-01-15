/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.select.analysis;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class QueryParamsExtractor {

    static final QueryParamsExtractor INSTANCE = new QueryParamsExtractor();

    private QueryParamsExtractor() {}

    List<Object> getParamsOf(QueryInfo query) {

        List<ParameterSetOperation> parameterSetOperations = retrieveParameterSetOperations(query);

        List<Object> paramsList = new ArrayList<>();
        for (ParameterSetOperation parameterSetOperation : parameterSetOperations) {
            Object[] paramsOfThisQuery = parameterSetOperation.getArgs();
            paramsList.add(paramsOfThisQuery[1]);
        }

        return paramsList;

    }

    private List<ParameterSetOperation> retrieveParameterSetOperations(QueryInfo query) {

        List<List<ParameterSetOperation>> allParametersLists = query.getParametersList();

        if(allParametersLists.isEmpty()) {
            return Collections.emptyList();
        }

        if(allParametersLists.size() > 1) {
            String message = "Several parameter set not managed, please create an issue"
                           + " on https://github.com/quick-perf/quickperf/issues describing your"
                           + " use case.";
            throw new IllegalStateException(message);
        }

        return allParametersLists.get(0);

    }

}
