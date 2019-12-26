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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.sql;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import net.ttddyy.dsproxy.listener.QueryUtils;

public class QueryTypeRetriever {

    public static final QueryTypeRetriever INSTANCE = new QueryTypeRetriever();

    private QueryTypeRetriever() { }

    public QueryType typeOf(QueryInfo query) {
        String queryAsString = query.getQuery();
        String trimmedQuery = QueryUtils.removeCommentAndWhiteSpace(queryAsString);

        // Because of ttddyy bug
        if (       trimmedQuery.startsWith("DROP")
                || trimmedQuery.startsWith("drop")) {
            return QueryType.OTHER;
        }

        return QueryUtils.getQueryType(trimmedQuery);
    }
}
