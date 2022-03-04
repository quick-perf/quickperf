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
package org.quickperf.sql.connection;

import org.quickperf.sql.connection.stack.StackTraceDisplayConfig;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionEventsProfiler extends ConnectionListener {

    private static final String CONNECTION_CLASS_NAME = "java.sql.Connection";

    private final ProfilingParameters profilingParameters;

    private final ConnectionProfiler profiler;

    public ConnectionEventsProfiler(ProfilingParameters profilingParameters) {
        this.profilingParameters = profilingParameters;
        PrintWriter printWriter = profilingParameters.getPrintWriter();
        StackTraceDisplayConfig stacktracedisplayConfig = profilingParameters.getStackTraceDisplayConfig();
        profiler = new ConnectionProfiler(stacktracedisplayConfig, printWriter);
    }

    public void start() {
        profiler.enables();
    }

    public void stop() {
        profiler.disables();
    }

    @Override
    public void theDatasourceGetsTheConnection(Connection connection) {
        profiler.profile(connection, "javax.sql.DataSource.getConnection()");
    }

    public void theDatasourceGetsTheConnectionWithUserNameAndPassword(Connection connection) {
        profiler.profile(connection, "javax.sql.DataSource.getConnection(String username, String password)");
    }

    @Override
    public void close(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("close()");
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void createStatement(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("createStatement");
        profileForTraceLevel(connection, eventDescription);
    }

    private void profileForTraceLevel(Connection connection, String text) {
        if(profilingParameters.getLevel() == Level.TRACE) {
            profiler.profile(connection, text);
        }
    }

    @Override
    public void prepareStatement(Connection connection, String sql) {
        String eventDescription =
                buildConnectionMethodDescription("prepareStatement(String sql)"
                                                , "sql"
                                                , sql);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareCall(Connection connection, String sql) {
        String eventDescription =
                buildConnectionMethodDescription("prepareCall(String sql)"
                                                , "sql"
                                                , sql);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void nativeSQL(Connection connection, String sql) {
        String eventDescription =
                buildConnectionMethodDescription("nativeSQL(String sql)"
                                                , "sql"
                                                , sql);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void setAutoCommit(Connection connection, boolean autoCommit) {
        String eventDescription =
                buildConnectionMethodDescription("setAutoCommit(boolean autoCommit)"
                                                , "autoCommit"
                                                , "" + autoCommit);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void commit(Connection connection) {
        String transactionIsolationAsString = extractTransactionIsolationOf(connection);
        String eventDescription =
                buildConnectionMethodDescription("commit()"
                                                , "isolation"
                                                , transactionIsolationAsString);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void rollback(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("rollback()");
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setReadOnly(Connection connection, boolean readOnly) {
        String eventDescription =
                buildConnectionMethodDescription("setReadOnly(boolean readOnly)"
                                                , "readOnly"
                                                , "" + readOnly);
        profiler.profile(connection,eventDescription);
    }

    @Override
    public void setCatalog(Connection connection, String catalog) {
        String eventDescription =
                buildConnectionMethodDescription("setCatalog(Connection connection, String catalog)"
                                                , "catalog"
                                                , "" + catalog);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void setTransactionIsolation(Connection connection, int level) {
        String transactionIsolationAsString = extractTransactionIsolationOf(connection);
        String eventDescription =
                buildConnectionMethodDescription("setTransactionIsolation(int level)"
                                                , "level"
                                                , "" + level + " (" + transactionIsolationAsString + ")");
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void clearWarnings(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("clearWarnings()");
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createStatement(Connection connection, int resultSetType, int resultSetConcurrency) {
        String methodNameWithArguments = "createStatement(int resultSetType, int resultSetConcurrency)";
        String[] additionalInfo = { "resultSetType" , "" + resultSetType
                                  , "resultSetConcurrency", "" + resultSetConcurrency
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency) {
        String methodNameWithArguments = "prepareStatement(String sql, int resultSetType, int resultSetConcurrency)";
        String[] additionalInfo = { "sql" , "" + sql
                                  , "resultSetType", "" + resultSetType
                                  , "resultSetConcurrency", "" + resultSetConcurrency
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection,  eventDescription);
    }

    @Override
    public void prepareCall(Connection connection, String sql, int resultSetType, int resultSetConcurrency) {
        String methodNameWithArguments = "prepareCall(String sql, int resultSetType, int resultSetConcurrency)";
        String[] additionalInfo = { "sql" , "" + sql
                                  , "resultSetType", "" + resultSetType
                                  , "resultSetConcurrency", "" + resultSetConcurrency
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void setTypeMap(Connection connection, Map<String, Class<?>> map) {
        String methodNameWithArguments = "setTypeMap(Map<String, Class<?>> map)";
        String[] additionalInfo = {"map", "" + map};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setHoldability(Connection connection, int holdability) {
        String methodNameWithArguments = "setHoldability(int holdability)";
        String[] additionalInfo = {"holdability", "" + holdability};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setSavepoint(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("setSavepoint()");
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setSavepoint(Connection connection, String name) {
        String methodNameWithArguments = "setSavepoint(String name)";
        String[] additionalInfo = {"name", "" + name};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void rollback(Connection connection, Savepoint savepoint) {
        String methodNameWithArguments = "rollback(Savepoint savepoint)";
        String[] additionalInfo = {"savepoint", "" + savepoint};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void releaseSavepoint(Connection connection, Savepoint savepoint) {
        String methodNameWithArguments = "releaseSavepoint(Savepoint savepoint)";
        String[] additionalInfo = {"savepoint", "" + savepoint};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void createStatement(Connection connection, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        String methodNameWithArguments = "createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)";
        String[] additionalInfo = { "resultSetType", "" + resultSetType
                                  + "resultSetConcurrency", "" + resultSetConcurrency
                                  + "resultSetHoldability", "" + resultSetHoldability
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        String methodNameWithArguments = "prepareStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)";
        String[] additionalInfo = { "resultSetType", "" + resultSetType
                                  + "resultSetConcurrency", "" + resultSetConcurrency
                                  + "resultSetHoldability", "" + resultSetHoldability
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareCall(Connection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) {
        String methodNameWithArguments = "prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)";
        String[] additionalInfo = { "resultSetType", "" + resultSetType
                                  + "resultSetConcurrency", "" + resultSetConcurrency
                                  + "resultSetHoldability", "" + resultSetHoldability
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int autoGeneratedKeys) {
        String methodNameWithArguments = "prepareStatement(String sql, int autoGeneratedKeys)";
        String[] additionalInfo = { "sql", "" + sql
                                  + "autoGeneratedKeys", "" + autoGeneratedKeys
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareStatement(Connection connection, String sql, int[] columnIndexes) {
        String methodNameWithArguments = "prepareStatement(String sql, int[] columnIndexes)";
        String[] additionalInfo = { "sql", "" + sql
                                  + "columnIndexes", "" + Arrays.toString(columnIndexes)
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void prepareStatement(Connection connection, String sql, String[] columnNames) {
        String methodNameWithArguments = "prepareStatement(String sql, String[] columnNames)";
        String[] additionalInfo = { "sql", "" + sql
                                  + "columnNames", "" + Arrays.toString(columnNames)
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createClob(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("createClob()");
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createBlob(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("createBlob()");
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createNClob(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("createNClob()");
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createSQLXML(Connection connection) {
        String eventDescription =
                buildConnectionMethodDescription("createSQLXML()");
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void setClientInfo(Connection connection, String name, String value) {
        String methodNameWithArguments = "setClientInfo(String name, String value)";
        String[] additionalInfo = { "name", "" + name
                                  + "value", "" + value
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setClientInfo(Connection connection, Properties properties) {
        String methodNameWithArguments = "setClientInfo(Properties properties)";
        String[] additionalInfo = { "properties", "" + properties };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createArrayOf(Connection connection, String typeName, Object[] elements) {
        String methodNameWithArguments = "createArrayOf(String typeName, Object[] elements)";
        String[] additionalInfo = { "typeName", "" + typeName
                                  + "elements", "" + Arrays.toString(elements)
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void createStruct(Connection connection, String typeName, Object[] attributes) {
        String methodNameWithArguments = "createStruct(String typeName, Object[] attributes)";
        String[] additionalInfo = { "typeName", "" + typeName
                                  + "attributes", "" + Arrays.toString(attributes)
                                  };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profileForTraceLevel(connection, eventDescription);
    }

    @Override
    public void setSchema(Connection connection, String schema) {
        String methodNameWithArguments = "setSchema(String schema)";
        String[] additionalInfo = { "schema", "" + schema};
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void abort(Connection connection, Executor executor) {
        String eventDescription =
                buildConnectionMethodDescription("abort(Executor executor)");
        profiler.profile(connection, eventDescription);
    }

    @Override
    public void setNetworkTimeout(Connection connection, Executor executor, int milliseconds) {
        String methodNameWithArguments = "setNetworkTimeout(Executor executor, int milliseconds)";
        String[] additionalInfo = { "milliseconds", "" + milliseconds };
        String eventDescription =
                buildConnectionMethodDescription(methodNameWithArguments, additionalInfo);
        profiler.profile(connection, eventDescription);
    }

    private String extractTransactionIsolationOf(Connection connection) {
        try {
            int transactionIsolation = connection.getTransactionIsolation();
            return formatTransactionIsolationAsString(transactionIsolation);
        } catch (SQLException sqlException) {
            return "";
        }
    }

    private String buildConnectionMethodDescription(String methodNameWithArguments, String... additionalData) {
        String additionalInfo = buildAdditionalInfoAsString(additionalData);
        return CONNECTION_CLASS_NAME + "." + methodNameWithArguments
                + (additionalInfo.isEmpty() ? "" : " [" + additionalInfo + "]");
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

    private String buildAdditionalInfoAsString(String[] additionalData) {
        StringBuilder additionalInfoBuilder = new StringBuilder();
        boolean isLabel = true;
        for (int i = 0, lastDatumIndex = additionalData.length; i < lastDatumIndex; i++) {
            String labelOrDescription = additionalData[i];
            additionalInfoBuilder.append(labelOrDescription);
            if (isLabel) {
                additionalInfoBuilder.append(": ");
            } else if(i != additionalData.length -1){
                additionalInfoBuilder.append(", ");
            }
            isLabel = !isLabel;
        }
        return additionalInfoBuilder.toString();
    }

}
