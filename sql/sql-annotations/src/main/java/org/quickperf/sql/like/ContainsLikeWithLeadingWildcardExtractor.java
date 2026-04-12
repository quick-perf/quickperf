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
package org.quickperf.sql.like;

import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.sql.SqlExecution;
import org.quickperf.sql.SqlExecutions;

import java.util.List;

public class ContainsLikeWithLeadingWildcardExtractor implements ExtractablePerformanceMeasure<SqlExecutions, BooleanMeasure> {

    public static final ContainsLikeWithLeadingWildcardExtractor INSTANCE =
            new ContainsLikeWithLeadingWildcardExtractor();

    private ContainsLikeWithLeadingWildcardExtractor() { }

    @Override
    public BooleanMeasure extractPerfMeasureFrom(SqlExecutions sqlExecutions) {
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (hasLikeWithLeadingWildcard(query)) {
                    return BooleanMeasure.TRUE;
                }
            }
        }
        return BooleanMeasure.FALSE;
    }

    private boolean hasLikeWithLeadingWildcard(QueryInfo queryInfo) {
        String sql = queryInfo.getQuery();
        boolean inString = false;
        int bindParamIndex = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '\'') {
                // '' inside a string literal is an escaped quote, not a boundary
                if (inString && i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    inString = !inString;
                }
                continue;
            }

            if (inString) {
                continue;
            }

            if (c == '-' && i + 1 < sql.length() && sql.charAt(i + 1) == '-') {
                i = sql.indexOf('\n', i + 2);
                if (i == -1) {
                    // No newline: the line comment runs to end-of-string, so
                    // no further LIKE keyword can appear.
                    return false;
                }
                continue;
            }

            if (c == '/' && i + 1 < sql.length() && sql.charAt(i + 1) == '*') {
                int end = sql.indexOf("*/", i + 2);
                if (end == -1) {
                    // Unterminated block comment: the rest of the SQL is comment
                    // body, so no further LIKE keyword can appear.
                    return false;
                }
                i = end + 1;
                continue;
            }

            if (c == '?') {
                bindParamIndex++;
                continue;
            }

            if (c != 'L' && c != 'l' && c != 'I' && c != 'i') {
                continue;
            }

            int likeLen = likeKeywordLength(sql, i);
            if (likeLen > 0) {
                int afterLike = skipWhitespaceAndOpenParen(sql, i + likeLen);

                if (afterLike < sql.length()) {
                    char next = sql.charAt(afterLike);
                    if (next == '\'') {
                        if (afterLike + 1 < sql.length() && isWildcard(sql.charAt(afterLike + 1))) {
                            return true;
                        }
                    } else if (next == '?') {
                        // The '?' after LIKE has not been counted yet: the outer loop
                        // will reach it on a subsequent iteration. Its 1-based JDBC
                        // index is therefore bindParamIndex + 1.
                        if (hasWildcardParamValue(queryInfo, bindParamIndex + 1)) {
                            return true;
                        }
                    }
                }
                i += likeLen - 1;
            }
        }
        return false;
    }

    /**
     * Returns the keyword length (4 for LIKE, 5 for ILIKE) if a whole SQL keyword
     * starts at position {@code i}, or 0 otherwise. Both the character before and
     * after the candidate must be a non-identifier character (or string boundary)
     * to reject false matches on identifiers such as "DISLIKE", "LIKELIHOOD", or
     * "x_like".
     */
    private int likeKeywordLength(String sql, int i) {
        if (i > 0 && isSqlIdentifierChar(sql.charAt(i - 1))) {
            return 0;
        }
        if (isKeywordAt(sql, i, "ILIKE")) {
            return 5;
        }
        if (isKeywordAt(sql, i, "LIKE")) {
            return 4;
        }
        return 0;
    }

    private boolean isKeywordAt(String sql, int i, String keyword) {
        int len = keyword.length();
        if (!sql.regionMatches(true, i, keyword, 0, len)) {
            return false;
        }
        int end = i + len;
        return end >= sql.length() || !isSqlIdentifierChar(sql.charAt(end));
    }

    private static boolean isSqlIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    private int skipWhitespaceAndOpenParen(String sql, int i) {
        while (i < sql.length() && (sql.charAt(i) == ' ' || sql.charAt(i) == '\t'
                || sql.charAt(i) == '\n' || sql.charAt(i) == '\r' || sql.charAt(i) == '(')) {
            i++;
        }
        return i;
    }

    /**
     * Checks whether the bind parameter at the given 1-based JDBC position
     * starts with a wildcard ({@code %} or {@code _}).
     */
    private boolean hasWildcardParamValue(QueryInfo queryInfo, int paramPosition) {
        // Each element in getParametersList() represents one execution of the prepared statement
        for (List<ParameterSetOperation> params : queryInfo.getParametersList()) {
            String resolvedValue = null;
            for (ParameterSetOperation param : params) {
                Object[] args = param.getArgs();
                if (args.length > 1 && args[0] instanceof Integer
                        && ((Integer) args[0]) == paramPosition
                        && args[1] instanceof String) {
                    // Last-write-wins JDBC semantics: keep the last value set for this index
                    resolvedValue = (String) args[1];
                }
            }
            if (resolvedValue != null && !resolvedValue.isEmpty() && isWildcard(resolvedValue.charAt(0))) {
                return true;
            }
        }
        return false;
    }

    private boolean isWildcard(char c) {
        return c == '%' || c == '_';
    }

}
