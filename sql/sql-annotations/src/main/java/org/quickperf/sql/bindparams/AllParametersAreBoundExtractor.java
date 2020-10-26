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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AllParametersAreBoundExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final AllParametersAreBoundExtractor INSTANCE = new AllParametersAreBoundExtractor();
    private static final List<String> SQL_KEYS = List.of("where", "values", "set");

    private AllParametersAreBoundExtractor() {
    }

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (oneUnbindParameter(query)) {
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
        if (SQL_KEYS.stream().anyMatch(queryInLowerCase::contains)) {
            String[] splitWhere = queryInLowerCase.split("where");
            List<String> andOrParts = new ArrayList<>();
            for (String s : splitWhere) {
                String[] andOrPart = s.split(" and | or ");
                Collections.addAll(andOrParts, andOrPart);
            }
            for (String wherePart : andOrParts) {
                KeyWord keyWord = this.wherePartSpliter(wherePart);
                switch (keyWord) {
                    case IN:
                        if (this.isStatementWithInUnbindParameter(wherePart)) {
                            return true;
                        }
                        break;
                    case VALUES:
                        if (this.isStatementWithInsertUnbindParameter(wherePart)) {
                            return true;
                        }
                        break;
                    case SET:
                        if (this.isStatementWithUpdateUnbindParameter(wherePart)) {
                            return true;
                        }
                        break;
                    case BETWEEN:
                    case OTHER:
                        if (this.isStatementWithUnbindParameter(wherePart)) {
                            return true;
                        }
                }
            }
        }
        return false;
    }

    private boolean isStatementWithUpdateUnbindParameter(String wherePart) {
        String[] inPart = wherePart.split("set");
        String[] commaParts = inPart[1].split(",");
        for (String part : commaParts) {
            if (!part.contains("?")) {
                return true;
            }
        }
        return false;
    }

    private boolean isStatementWithInsertUnbindParameter(String wherePart) {
        String[] inPart = wherePart.split("values");
        String[] commaParts = inPart[1].split(",");
        for (String part : commaParts) {
            if (!part.contains("?")) {
                return true;
            }
        }
        return false;
    }

    private KeyWord wherePartSpliter(String word) {
        for (KeyWord keyWord : KeyWord.values()){
            if(word.split(keyWord.getValue()).length > 1){
                return keyWord;
            }
        }
        return KeyWord.OTHER;
    }

    private boolean isStatementWithInUnbindParameter(String wherePart) {
        //return false while 'IN' referenced nested statement
        if (wherePart.contains("select")) {
            return false;
        }
        String[] inPart = wherePart.split("in");
        String[] commaParts = inPart[1].split(",");
        for (String part : commaParts) {
            if (!part.contains("?")) {
                return true;
            }
        }
        return false;
    }

    private boolean isStatementWithUnbindParameter(String wherePart) {
        wherePart = wherePart.replaceAll(" ", "");
        //return false while 'WHERE' clause referenced nested statement
        if (wherePart.contains("select")) {
            return false;
        }
        return !wherePart.contains("?");
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
