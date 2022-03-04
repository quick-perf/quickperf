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
        if (SqlKeyWord.hasConditions(queryInLowerCase)) {
            List<String> conditions = SqlKeyWord.extractConditions(queryInLowerCase);
            for (String condition : conditions) {
                SqlKeyWord sqlKeyWord = SqlKeyWord.wherePartSplitter(condition);
                if (sqlKeyWord.hasUnBindParameter(condition)) {
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
                if (isReferencedNestedStatement(andOrPart)) {
                    return false;
                }
                return hasUnBindParameter(IN, andOrPart);
            }
        },
        VALUES("values") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                return hasUnBindParameter(VALUES, andOrPart);
            }
        },
        SET("set") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                return hasUnBindParameter(SET, andOrPart);
            }
        },
        OTHER("other") {
            @Override
            boolean hasUnBindParameter(String andOrPart) {
                andOrPart = andOrPart.replaceAll(" ", "");
                if (isReferencedNestedStatement(andOrPart)) {
                    return false;
                }

                if(isJoinWithWhereUsed(andOrPart)) {
                    return false;
                }

                return isUnbindParameter(andOrPart);
            }

            // Example WHERE a.title=b.title
            private boolean isJoinWithWhereUsed(final String andOrPart) {
                String[] equalParts = andOrPart.split("=");
                return equalParts[0].contains(".") && equalParts.length == 2 && equalParts[1].contains(".");
            }
        };

        private static boolean isUnbindParameter(String parameter) {
            return !parameter.contains("?");
        }

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

        static boolean hasConditions(String queryInLowerCase) {
            return queryInLowerCase.contains("where") || queryInLowerCase.contains("values") || queryInLowerCase.contains("set");
        }

        static List<String> extractConditions(String queryInLowerCase) {
            String[] whereParts = queryInLowerCase.split("where");
            List<String> conditions = new ArrayList<>();
            for (String wherePart : whereParts) {
                String[] andOrPart = wherePart.split(" and | or ");
                Collections.addAll(conditions, andOrPart);
            }
            return conditions;
        }

        static boolean hasUnBindParameter(final SqlKeyWord sqlKeyWord, final String andOrPart) {
            String[] sqlKeyWordParts = andOrPart.split(sqlKeyWord.getValue());
            String[] commaParts = sqlKeyWordParts[1].split(",");
            for (String part : commaParts) {
                if (isUnbindParameter(part)) {
                    return true;
                }
            }
            return false;
        }

        abstract boolean hasUnBindParameter(String andOrPart);
    }
}