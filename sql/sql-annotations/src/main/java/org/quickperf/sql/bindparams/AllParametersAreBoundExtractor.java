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
        if (queryInLowerCase.contains("where") || queryInLowerCase.contains("values") || queryInLowerCase.contains("set")) {
            String[] splitWhere = queryInLowerCase.split("where");
            List<String> andOrParts = new ArrayList<>();
            for (String s : splitWhere) {
                String[] andOrPart = s.split(" and | or ");
                Collections.addAll(andOrParts, andOrPart);
            }
            for (String andOrPart : andOrParts) {
                SqlKeyWord sqlKeyWord = SqlKeyWord.wherePartSplitter(andOrPart);
                if (sqlKeyWord.hasUnBindParameter(andOrPart)) {
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

    private enum SqlKeyWord {
        IN("in") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                if (SqlKeyWord.isReferencedNestedStatement(andOrPart)) {
                    return false;
                }
                String[] inPart = andOrPart.split(SqlKeyWord.IN.getValue());
                String[] commaParts = inPart[1].split(",");
                for (String part : commaParts) {
                    if (!part.contains("?")) {
                        return true;
                    }
                }
                return false;
            }
        },
        VALUES("values") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                String[] inPart = andOrPart.split(SqlKeyWord.VALUES.getValue());
                String[] commaParts = inPart[1].split(",");
                for (String part : commaParts) {
                    if (!part.contains("?")) {
                        return true;
                    }
                }
                return false;
            }
        },
        SET("set") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                String[] inPart = andOrPart.split(SqlKeyWord.SET.getValue());
                String[] commaParts = inPart[1].split(",");
                for (String part : commaParts) {
                    if (!part.contains("?")) {
                        return true;
                    }
                }
                return false;
            }
        },
        OTHER("other") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                andOrPart = andOrPart.replaceAll(" ", "");
                if (SqlKeyWord.isReferencedNestedStatement(andOrPart)) {
                    return false;
                }
                return !andOrPart.contains("?");
            }
        };
        private final String keyWord;

        SqlKeyWord(String keyWord) {
            this.keyWord = keyWord;
        }

        static SqlKeyWord wherePartSplitter(String word) {
            for (SqlKeyWord sqlKeyWord : SqlKeyWord.values()) {
                if (word.split(" " + sqlKeyWord.getValue() + " ").length > 1) {
                    return sqlKeyWord;
                }
            }
            return SqlKeyWord.OTHER;
        }

        public String getValue() {
            return keyWord;
        }

        static boolean isReferencedNestedStatement(String andOrPart){
            return andOrPart.contains("select") || andOrPart.contains("delete");
        }
        abstract boolean hasUnBindParameter(String andOrPart);
    }
}