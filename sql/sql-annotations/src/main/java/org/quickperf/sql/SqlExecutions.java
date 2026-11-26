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
package org.quickperf.sql;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import org.quickperf.SystemProperties;
import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.PerfIssuesFormat;
import org.quickperf.perfrecording.SelfVerifiablePerfRecord;
import org.quickperf.perfrecording.ViewablePerfRecordIfPerfIssue;
import org.quickperf.sql.framework.quickperf.DataSourceConfig;
import org.quickperf.sql.update.columns.NumberOfUpdatedColumnsStatistics;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class SqlExecutions implements Iterable<SqlExecution>, ViewablePerfRecordIfPerfIssue, SelfVerifiablePerfRecord, Serializable {

    private static final long serialVersionUID = 1L;

    public static final SqlExecutions NONE = new SqlExecutions();

    /** Description of the perf issue failing a test whose measure was
     * recorded under a cross-test contamination scenario (SQL executed on a
     * thread that is not a test thread while two or more tests were
     * recording SQL). See {@link #verifyRecord()}. */
    public static final String CROSS_TEST_CONTAMINATION_MESSAGE =
              "Cross-test SQL contamination: QuickPerf cannot reliably attribute the SQL recorded for this test."
            + System.lineSeparator()
            + "       " + "SQL was executed on a thread that is not a QuickPerf test thread (a worker thread such as"
            + " @Async, @Scheduled, Tomcat HTTP, JMS / Kafka listener, gRPC, virtual thread, ..., a thread started by a test,"
            + " or the thread of a test not run by QuickPerf) while several QuickPerf tests were recording SQL,"
            + " so this SQL was recorded for each of them."
            + System.lineSeparator()
            + "       " + "The SQL properties of this test cannot be verified, even if they look respected."
            + System.lineSeparator()
            + "       " + "Run this test in isolation (disable parallel execution), or annotate it with @HeapSize / @Xmx"
            + " (or another JVM-forking annotation) to force a dedicated JVM; the QuickPerf JUnit 6 extension"
            + " (quick-perf-junit6) and QuickPerfSqlTestNGListener do not support these annotations.";

    private final Deque<SqlExecution> sqlExecutions = new ConcurrentLinkedDeque<SqlExecution>();

    /** O(1) size counter. ConcurrentLinkedDeque#size is O(n); we maintain an AtomicInteger
     * incremented after addLast(...) so {@link #getNumberOfExecutions()} is constant time and
     * thread-safe under concurrent writers. The pinned write order (addLast first, then
     * incrementAndGet) keeps the counter as a lower bound on the deque size during the
     * tearing window — never an upper bound. Not serialized: a record can be serialized while
     * a worker thread adds an execution, so the counter is computed again from the deserialized
     * executions, see {@link #readObject(ObjectInputStream)}. */
    private transient AtomicInteger executionCount = new AtomicInteger();

    /** True when this measure was tagged by
     * {@link #markCrossTestContamination()} after the recorder it was
     * extracted from was observed in a multi-owner active-set fallback. The
     * tag is turned into a failing perf issue by {@link #verifyRecord()}.
     * NONE is guarded so the shared singleton can never carry a
     * contamination tag. */
    private volatile boolean crossTestContamination;

    public void add(ExecutionInfo execInfo, List<QueryInfo> queries) {
        if (this == NONE) {
            // Defence-in-depth: NONE is a JVM-wide-shared singleton; never mutate it.
            return;
        }
        SqlExecution sqlExecution = new SqlExecution(execInfo, queries);
        sqlExecutions.addLast(sqlExecution);
        executionCount.incrementAndGet();
    }

    // Workaround: avoid duplicate call to retrieveNumberOfReturnedColumns within SqlExecution constructor
    // in add method by forcing the column count. Related commit https://github.com/quick-perf/quickperf/issues/141
    private void addWithoutCall(ExecutionInfo executionInfo, List<QueryInfo> queries){
        if (this == NONE) {
            return;
        }
        SqlExecution sqlExecution = new SqlExecution(executionInfo, queries, 0);
        sqlExecutions.addLast(sqlExecution);
        executionCount.incrementAndGet();
    }

    public SqlExecutions filterByQueryType(QueryType queryType) {
        SqlExecutions filteredSqlExecutions = new SqlExecutions();

        for (SqlExecution execution : this.sqlExecutions) {
            List<QueryInfo> queries = new ArrayList<>();
            boolean added = false;

            for (QueryInfo query : execution.getQueries()) {
                if (queryType.equals(QueryTypeRetriever.INSTANCE.typeOf(query))) {
                    added = true;
                    queries.add(query);
                }
            }

            if(added){
                filteredSqlExecutions.addWithoutCall(execution.getExecutionInfo(), queries);
            }
        }
        return filteredSqlExecutions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SqlExecution sqlExecution : sqlExecutions) {
            sb.append("\t").append(sqlExecution.toString());
            sb.append(System.lineSeparator());
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return this == NONE || sqlExecutions.isEmpty();
    }

    public int retrieveQueryNumberOfType(QueryType queryType) {
        int queryNumber = 0;
        QueryTypeRetriever queryTypeRetriever = QueryTypeRetriever.INSTANCE;
        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                if (queryType.equals(queryTypeRetriever.typeOf(query))) {
                    queryNumber++;
                }
            }
        }
        return queryNumber;
    }

    public NumberOfUpdatedColumnsStatistics getUpdatedColumnsStatistics() {

        long minColumnCount = 0;
        long maxColumnCount = 0;

        for (SqlExecution sqlExecution : sqlExecutions) {
            for (QueryInfo query : sqlExecution.getQueries()) {
                QueryTypeRetriever queryTypeRetriever = QueryTypeRetriever.INSTANCE;
                if (queryTypeRetriever.typeOf(query) == QueryType.UPDATE) {
                    long updatedColumnCount = countUpdatedColumn(query.getQuery());
                    if(minColumnCount == 0 || updatedColumnCount < minColumnCount) {
                        minColumnCount = updatedColumnCount;
                    }
                    if (updatedColumnCount > maxColumnCount) {
                        maxColumnCount = updatedColumnCount;
                    }
                }
            }
        }

        return new NumberOfUpdatedColumnsStatistics(minColumnCount, maxColumnCount);

    }

    private long countUpdatedColumn(String sql) {
        // UPDATE book SET isbn = ?, title = ? WHERE id = ?
        int setIndex = sql.toLowerCase().indexOf("set");
        int whereIndex = sql.toLowerCase().indexOf("where");
        whereIndex = whereIndex > -1 ? whereIndex : sql.length();

        String sqlSetClause = sql.substring(setIndex, whereIndex);
        return countUnquotedEquals(sqlSetClause);
    }

    /**
     * Examples :
     *  - "SET isbn = ?, title = ? " returns 2
     *  - "SET isbn = '123', title = '1 + 1 = 0' " returns 2
     */
    private long countUnquotedEquals(String setClause) {
        boolean inQuote = false;
        long equalCounter = 0;
        for (char c : setClause.toCharArray()) {
            if (c == '\'') {
               inQuote = !inQuote;
            }
            if (!inQuote && c == '=') {
                equalCounter++;
            }
        }
        return equalCounter;
    }

    public long getMaxNumberOfSelectedColumns() {
        long maxNumberOfColumnsForAllExecs = 0;
        for (SqlExecution sqlExecution : sqlExecutions) {
            long columnCount = sqlExecution.getColumnCount();
            if (columnCount > maxNumberOfColumnsForAllExecs) {
                maxNumberOfColumnsForAllExecs = columnCount;
            }
        }
        return maxNumberOfColumnsForAllExecs;
    }

    @Override
    public String format(Collection<PerfIssue> perfIssues) {
        String standardFormatting = PerfIssuesFormat.STANDARD.format(perfIssues);

        if(SystemProperties.SIMPLIFIED_SQL_DISPLAY.evaluate()) {
            return standardFormatting;
        }

        return    standardFormatting
                + System.lineSeparator()
                + System.lineSeparator()
                + "[JDBC QUERY EXECUTION (executeQuery, executeBatch, ...)]"
                + System.lineSeparator()
                + (noJdbcExecution() ? new DataSourceConfig().getMessage()
                                     : toString());
    }

    /** Tag this measure as observed under cross-test contamination. NONE is
     * a JVM-wide-shared singleton; tagging it would fail every test that
     * subsequently reads NONE, so the call is silently ignored on NONE. */
    public void markCrossTestContamination() {
        if (this == NONE) {
            return;
        }
        crossTestContamination = true;
    }

    /** True when this measure has been tagged via
     * {@link #markCrossTestContamination()}. */
    public boolean hasCrossTestContamination() {
        return crossTestContamination;
    }

    /** A measure recorded under cross-test contamination may include SQL of
     * a sibling test: the test fails with
     * {@link #CROSS_TEST_CONTAMINATION_MESSAGE}, even if its SQL annotations
     * are respected. */
    @Override
    public PerfIssue verifyRecord() {
        if (crossTestContamination) {
            return new PerfIssue(CROSS_TEST_CONTAMINATION_MESSAGE);
        }
        return PerfIssue.NONE;
    }

    // The executions added so far, and the contamination tag: the executions
    // added afterwards are not in the copy. Never NONE, so that the copy can
    // be tagged.
    SqlExecutions copy() {
        SqlExecutions copy = new SqlExecutions();
        for (SqlExecution sqlExecution : sqlExecutions) {
            copy.sqlExecutions.addLast(sqlExecution);
            copy.executionCount.incrementAndGet();
        }
        copy.crossTestContamination = crossTestContamination;
        return copy;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        executionCount = new AtomicInteger(sqlExecutions.size());
    }

    private boolean noJdbcExecution() {
        return executionCount.get() == 0;
    }

    @Override
    public Iterator<SqlExecution> iterator() {
        return sqlExecutions.iterator();
    }

    public int getNumberOfExecutions() {
        return executionCount.get();
    }

}
