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

package org.quickperf.sql;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.QueryType;
import net.ttddyy.dsproxy.StatementType;
import net.ttddyy.dsproxy.proxy.ParameterSetOperation;
import org.quickperf.sql.formatter.QuickPerfSqlFormatter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlExecution implements Externalizable {

    private ExecutionInfo executionInfo;

    private List<QueryInfo> queries;

    private long columnCount;

    // Necessary for serialization
    public SqlExecution() {}

    public SqlExecution(ExecutionInfo executionInfo, List<QueryInfo> queries) {
        this.executionInfo = executionInfo;
        this.queries = queries;

        // Exception with H2 if column count is retrieved from
        // MaxSelectedColumnsPerMeasureExtractor
        if (atLeastOneSelect(queries)) {
            this.columnCount = retrieveNumberOfReturnedColumns(executionInfo);
        }
    }

    private boolean atLeastOneSelect(List<QueryInfo> queries) {
        QueryTypeRetriever queryTypeRetriever = QueryTypeRetriever.INSTANCE;
        for (QueryInfo query : queries) {
            QueryType queryType = queryTypeRetriever.typeOf(query);
            if (queryType == QueryType.SELECT) {
                return true;
            }
        }
        return false;
    }

    private long retrieveNumberOfReturnedColumns(ExecutionInfo executionInfo) {
        if (dbExceptionHappened(executionInfo) || executeMethodOnStatement(executionInfo)) {
            return 0;
        }
        ResultSet resultSet = (ResultSet) executionInfo.getResult();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            return metaData.getColumnCount();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private boolean dbExceptionHappened(ExecutionInfo executionInfo) {
        return executionInfo.getResult() == null;
    }

    private boolean executeMethodOnStatement(ExecutionInfo executionInfo) {
        return !(executionInfo.getResult() instanceof ResultSet);
    }

    public boolean hasQueryFollowing(SqlQueryPredicate sqlQueryPredicate) {
        if (queries == null) {
            return false;
        }
        for (QueryInfo queryInfo : queries) {
            if (sqlQueryPredicate.test(queryInfo.getQuery())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        writeExecutionInfo(out);

        out.writeLong(columnCount);

        writeQueries(out);

    }

    private void writeExecutionInfo(ObjectOutput out) throws IOException {
        out.writeUTF(executionInfo.getDataSourceName());
        out.writeUTF(executionInfo.getConnectionId());
        out.writeUTF(executionInfo.getStatementType().name());
        out.writeBoolean(executionInfo.isBatch());
        out.writeInt(executionInfo.getBatchSize());
    }

    private void writeQueries(ObjectOutput out) throws IOException {

        int numberOfQueries = queries.size();
        out.writeInt(numberOfQueries);

        for (QueryInfo query : queries) {
            writeQuery(out, query);
        }
    }

    private void writeQuery(ObjectOutput out, QueryInfo query) throws IOException {

        String queryAsString = query.getQuery();
        out.writeUTF(queryAsString);

        List<List<ParameterSetOperation>> parametersList = query.getParametersList();

        int numberOfParametersList = parametersList.size();
        out.writeInt(numberOfParametersList);

        for (List<ParameterSetOperation> parameters : parametersList) {

            int numberOfParams = parameters.size();
            out.writeInt(numberOfParams);

            for (ParameterSetOperation parameter : parameters) {
                writeParameter(out, parameter);
            }

        }
    }

    private void writeParameter(ObjectOutput out, ParameterSetOperation parameter) throws IOException {
        Method method = parameter.getMethod();
        writeMethod(out, method);

        Object[] args = parameter.getArgs();
        writeArgs(out, args);
    }

    private void writeMethod(ObjectOutput out, Method method) throws IOException {
        out.writeObject(method.getDeclaringClass());
        out.writeUTF(method.getName());
        out.writeObject(method.getParameterTypes());
    }

    private void writeArgs(ObjectOutput out, Object[] args) throws IOException {
        int numberOfArgs = args.length;
        out.writeInt(numberOfArgs);
        for (Object arg : args) {
            out.writeObject(arg);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.executionInfo = readExecutionInfo(in);
        this.columnCount = in.readLong();
        this.queries = readQueries(in);
    }

    private ExecutionInfo readExecutionInfo(ObjectInput in) throws IOException {
        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setDataSourceName(in.readUTF());
        executionInfo.setConnectionId(in.readUTF());
        executionInfo.setStatementType(StatementType.valueOf(in.readUTF()));
        executionInfo.setBatch(in.readBoolean());
        executionInfo.setBatchSize(in.readInt());
        return executionInfo;
    }

    private List<QueryInfo> readQueries(ObjectInput in) throws IOException, ClassNotFoundException {
        int numberOfQueries = in.readInt();
        List<QueryInfo> queries = new ArrayList<>(numberOfQueries);
        for (int i = 0; i < numberOfQueries; i++) {
            QueryInfo query = readQuery(in);
            queries.add(query);
        }
        return queries;
    }

    private QueryInfo readQuery(ObjectInput in) throws IOException, ClassNotFoundException {
        QueryInfo query = new QueryInfo();
        query.setQuery(in.readUTF());

        int numberOfParametersList = in.readInt();
        List<List<ParameterSetOperation>> parametersList = new ArrayList<>(numberOfParametersList);

        for (int j = 0; j < numberOfParametersList; j++) {

            int numberOfParams = in.readInt();
            List<ParameterSetOperation> parameters = new ArrayList<>(numberOfParams);
            for (int k = 0; k < numberOfParams; k++) {

                ParameterSetOperation parameterSetOperation = new ParameterSetOperation();

                Method method = readMethod(in);
                parameterSetOperation.setMethod(method);

                Object[] args = readArgs(in);
                parameterSetOperation.setArgs(args);

                parameters.add(parameterSetOperation);

            }

            parametersList.add(parameters);

        }

        query.setParametersList(parametersList);
        return query;
    }

    private Method readMethod(ObjectInput in) throws ClassNotFoundException, IOException {
        Class<?> declaringClass = (Class<?>) in.readObject();
        String methodName = in.readUTF();
        Class<?>[] parameterTypes = (Class<?>[]) in.readObject();

        Method method;
        try {
            method = declaringClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        return method;
    }

    private Object[] readArgs(ObjectInput in) throws IOException, ClassNotFoundException {
        int numberOfArgs = in.readInt();
        Object[] args = new Object[numberOfArgs];
        for (int l = 0; l < numberOfArgs; l++) {
            Object object = in.readObject();
            args[l] = object;
        }
        return args;
    }

    public List<QueryInfo> getQueries() {
        return queries;
    }

    public long getColumnCount() {
        return columnCount;
    }
    
    public long getElapsedTime() {
    	return executionInfo.getElapsedTime();
    }

    public boolean withStatement() {
        StatementType statementType = executionInfo.getStatementType();
        return StatementType.STATEMENT.equals(statementType);
    }

    @Override
    public String toString() {
        return QuickPerfSqlFormatter.INSTANCE.format(executionInfo, queries);
    }

}