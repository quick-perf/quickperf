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

package org.quickperf.sql.connection;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionEventsProfiler extends ConnectionListener {

    private final ProfilingParameters profilingParameters;

    private final ConnectionProfiler profiler;

    public void start() {
        profiler.enables();
    }

    public void stop() {
        profiler.disables();
    }

    public ConnectionEventsProfiler(ProfilingParameters profilingParameters) {
        this.profilingParameters = profilingParameters;
        PrintWriter printWriter = profilingParameters.getPrintWriter();
        StackTraceDisplayConfig stacktracedisplayConfig = profilingParameters.getStackTraceDisplayConfig();
        profiler = new ConnectionProfiler(stacktracedisplayConfig, printWriter);
    }

    @Override
    public void theDatasourceGetsTheConnection(Connection connection) {
        profiler.profile(connection, "connection gotten from the datasource");
    }

    private void profileForTraceLevel(Connection connection, String text) {
        if(profilingParameters.getLevel() == Level.TRACE) {
            profiler.profile(connection, text);
        }
    }

    @Override
    public void commit(Connection connection) {
        String transactionIsolationAsString = extractTransactionIsolationOf(connection);
        profiler.profile(connection,"commit with " + transactionIsolationAsString + " isolation");
    }

    @Override
    public void close(Connection connection) {
        profiler.profile(connection, "closed");
    }

    @Override
    public void setReadOnly(Connection connection, boolean readOnly) {
        profiler.profile(connection,"read only set to " + readOnly);
    }

    @Override
    public void createStatement(Connection connection) {
        profileForTraceLevel(connection, "create statement");
    }

    @Override
    public void prepareStatement(Connection connection, String sql) {
        profileForTraceLevel(connection, "prepare statement with " + sql + " (SQL)");
    }

    @Override
    public void prepareCall(Connection connection, String sql) {
        profileForTraceLevel(connection, "prepare callable statement with " + sql + " (SQL)");
    }
    @Override
    public void nativeSQL(Connection connection, String sql) {
        profileForTraceLevel(connection, "native SQL " + sql);
    }

    @Override
    public void setAutoCommit(Connection connection, boolean autoCommit) {
        profiler.profile(connection, "auto commit set to " + autoCommit);
    }

    @Override
    public void rollback(Connection connection) {
        profiler.profile(connection, "rollback");
    }

    @Override
    public void setCatalog(Connection connection, String catalog) {
        profiler.profile(connection, "set catalog: " + catalog);
    }

    @Override
    public void setTransactionIsolation(Connection connection, int level) {
        String transactionIsolationAsString = extractTransactionIsolationOf(connection);
        profiler.profile(connection, "set transaction isolation to " + level + " (" + transactionIsolationAsString + ")");
    }

    @Override
    public void clearWarnings(Connection connection) {
        profileForTraceLevel(connection, "clear warnings");
    }

    @Override
    public void createStatement(Connection connection, int resultSetType, int resultSetConcurrency) {
        profileForTraceLevel(connection, "create statement with " + resultSetType + " (resultSetType), " + resultSetConcurrency + " (resultSetConcurrency)");
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency) {
        profileForTraceLevel(connection, "prepare statement with " + sql + "(SQL), " + resultSetType + " (resultSetType), " + resultSetConcurrency + " (resultSetConcurrency)");
        super.prepareStatement(connection, sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public void prepareCall(Connection connection, String sql, int resultSetType, int resultSetConcurrency) {
        profileForTraceLevel(connection, "prepare call with " + sql + "(SQL), " + resultSetType + " (resultSetType), " + resultSetConcurrency + " (resultSetConcurrency)");
    }

    @Override
    public void setTypeMap(Connection connection, Map<String, Class<?>> map) {
        profileForTraceLevel(connection, "type map set to " + map);
    }

    @Override
    public void setHoldability(Connection connection, int holdability) {
        profileForTraceLevel(connection, "holdability set to " + holdability);
    }

    @Override
    public void setSavepoint(Connection connection) {
        profileForTraceLevel(connection, "set save point");
    }

    @Override
    public void setSavepoint(Connection connection, String name) {
        profileForTraceLevel(connection, "set save point with " + name + " (name)");
    }

    @Override
    public void rollback(Connection connection, Savepoint savepoint) {
        profiler.profile(connection, "set rollback with " + savepoint + " (save point)");
    }

    @Override
    public void releaseSavepoint(Connection connection, Savepoint savepoint) {
        profiler.profile(connection, "release " + savepoint + " (save point)");
    }

    @Override
    public void createStatement(Connection connection, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        profileForTraceLevel(connection, "create statement with " + resultSetType + "(resultSetType), " + resultSetConcurrency + " (resultSetConcurrency), " + resultSetHoldability + " (resultSetHoldability)");
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        profileForTraceLevel(connection, "prepare statement with " + sql + "(sql), " + resultSetType + " (resultSetType), "
                + resultSetConcurrency + " (resultSetConcurrency), " + resultSetHoldability + " (resultSetHoldability)");
    }

    @Override
    public void prepareCall(Connection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        profileForTraceLevel(connection, "prepare callable statement with " + sql + "(sql), " + resultSetType + " (resultSetType), "
                + resultSetConcurrency + " (resultSetConcurrency), " + resultSetHoldability + " (resultSetHoldability)");
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int autoGeneratedKeys) {
        profileForTraceLevel(connection, "prepare callable statement with " + sql + "(sql), " + autoGeneratedKeys + " (autoGeneratedKeys)");
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int[] columnIndexes) {
        profileForTraceLevel(connection, "prepare statement with " + sql + "(sql), " + Arrays.toString(columnIndexes) + " (columnIndexes)");
    }

    @Override
    public void prepareStatement(Connection connection, String sql, String[] columnNames) {
        profileForTraceLevel(connection, "prepare statement with " + sql + "(sql), " + Arrays.toString(columnNames) + " (columnNames)");
    }

    @Override
    public void createClob(Connection connection) {
        profileForTraceLevel(connection, "create CLOB");
    }

    @Override
    public void createBlob(Connection connection) {
        profileForTraceLevel(connection, "create BLOB");
    }

    @Override
    public void createNClob(Connection connection) {
        profileForTraceLevel(connection, "create N CLOB");
    }

    @Override
    public void createSQLXML(Connection connection) {
        profileForTraceLevel(connection, "create SQL XML");
    }

    @Override
    public void setClientInfo(Connection connection, String name, String value) {
        profileForTraceLevel(connection, "set client info to " + name + "(name) and " + value + " (value)");
    }

    @Override
    public void setClientInfo(Connection connection, Properties properties) {
        profileForTraceLevel(connection, "set client info to " + properties + "(properties)");
    }

    @Override
    public void createArrayOf(Connection connection, String typeName, Object[] elements) {
        profileForTraceLevel(connection, "create array of with " + typeName + " (type name) and " + Arrays.toString(elements) + " (elements)");
    }

    @Override
    public void createStruct(Connection connection, String typeName, Object[] attributes) {
        profileForTraceLevel(connection, "create struct with " + typeName + " (type name) and " + Arrays.toString(attributes) + " (attributes)");
    }

    @Override
    public void setSchema(Connection connection, String schema) {
        profileForTraceLevel(connection, "set schema to " + schema);
    }

    @Override
    public void abort(Connection connection, Executor executor) {
        profiler.profile(connection, "set abort with executor");
    }

    @Override
    public void setNetworkTimeout(Connection connection, Executor executor, int milliseconds) {
        profiler.profile(connection, "set network timeout to " + milliseconds + " (milliseconds) with executor");
    }

    private String extractTransactionIsolationOf(Connection connection) {
        try {
            int transactionIsolation = connection.getTransactionIsolation();
            return formatTransactionIsolationAsString(transactionIsolation);
        } catch (SQLException sqlException) {
            return "";
        }
    }

    private String formatTransactionIsolationAsString(int transactionIsolation) {
        if(transactionIsolation == 0) {
            return "transaction_none";
        } else if(transactionIsolation == 1) {
            return "transaction_read_uncommitted";
        } else if (transactionIsolation == 2) {
            return "transaction_read_committed";
        } else if(transactionIsolation == 8) {
            return "transaction_serializable";
        }
        return "";
    }

}
