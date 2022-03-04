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

import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class QuickPerfDatabaseConnection implements Connection {

    private final Connection delegate;

    private Collection<ConnectionListener> connectionListeners;

    private QuickPerfDatabaseConnection(Connection connection) {
        this.delegate = connection;
    }

    public static QuickPerfDatabaseConnection buildFrom(Connection connection) {
        QuickPerfDatabaseConnection quickPerfDatabaseConnection = new QuickPerfDatabaseConnection(connection);
        quickPerfDatabaseConnection.connectionListeners =
                ConnectionListenerRegistry.INSTANCE.getConnectionListeners();
        return quickPerfDatabaseConnection;
    }

    public void theDatasourceGetsTheConnection() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.theDatasourceGetsTheConnection(this);
        }
    }

    public void theDatasourceGetsTheConnectionWithUserNameAndPassword() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.theDatasourceGetsTheConnectionWithUserNameAndPassword(this);
        }
    }

    @Override
    public Statement createStatement() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createStatement(this);
        }
        return delegate.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql);
        }
        return delegate.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareCall(this, sql);
        }
        return delegate.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.nativeSQL(this, sql);
        }
        return delegate.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setAutoCommit(this, autoCommit);
        }
        delegate.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return delegate.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.commit(this);
        }
        delegate.commit();
    }

    @Override
    public void rollback() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.rollback(this);
        }
        delegate.rollback();
    }

    @Override
    public void close() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.close(this);
        }
        delegate.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setReadOnly(this, readOnly);
        }
        delegate.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return delegate.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setCatalog(this, catalog);
        }
        delegate.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return delegate.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setTransactionIsolation(this, level);
        }
        delegate.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return delegate.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.clearWarnings(this);
        }
        delegate.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createStatement(this, resultSetType, resultSetConcurrency);
        }
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql, resultSetType, resultSetConcurrency);
        }
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareCall(this, sql, resultSetType, resultSetConcurrency);
        }
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return delegate.getTypeMap();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setTypeMap(this, map);
        }
        delegate.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setHoldability(this, holdability);
        }
        delegate.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setSavepoint(this);
        }
        return delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setSavepoint(this, name);
        }
        return delegate.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.rollback(this, savepoint);
        }
        delegate.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.releaseSavepoint(this, savepoint);
        }
        delegate.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability) ;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability) ;
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareCall(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        }
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability) ;
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql, autoGeneratedKeys);
        }
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql, columnIndexes);
        }
        return delegate.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.prepareStatement(this, sql, columnNames);
        }
        return delegate.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createClob(this);
        }
        return delegate.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createBlob(this);
        }
        return delegate.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createNClob(this);
        }
        return delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createSQLXML(this);
        }
        return delegate.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return delegate.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setClientInfo(this, name, value);
        }
        delegate.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setClientInfo(this, properties);
        }
        delegate.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return delegate.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return delegate.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createArrayOf(this, typeName, elements);
        }
        return delegate.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.createStruct(this, typeName, attributes);
        }
        return delegate.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setSchema(this, schema);
        }
        delegate.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return delegate.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.abort(this, executor);
        }
        delegate.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.setNetworkTimeout(this, executor, milliseconds);
        }
        delegate.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return delegate.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

}
