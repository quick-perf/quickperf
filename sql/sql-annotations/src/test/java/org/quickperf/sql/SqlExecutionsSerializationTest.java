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
import net.ttddyy.dsproxy.StatementType;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// A test executed in a forked JVM serializes its SQL executions, which the
// JVM that forked it deserializes.
public class SqlExecutionsSerializationTest {

    @Test public void
    should_keep_the_executions_once_deserialized() throws Exception {

        // GIVEN
        SqlExecutions sqlExecutions = new SqlExecutions();
        sqlExecutions.add(anExecution(), anInsert());
        sqlExecutions.add(anExecution(), anInsert());

        // WHEN
        SqlExecutions deserializedSqlExecutions = serializeAndDeserialize(sqlExecutions);

        // THEN
        assertThat(deserializedSqlExecutions).hasSize(2);
        assertThat(deserializedSqlExecutions.getNumberOfExecutions()).isEqualTo(2);

    }

    @Test public void
    should_count_the_executions_it_holds_once_deserialized() throws Exception {

        // GIVEN executions serialized while a worker thread adds one: the
        // execution is added before the count is incremented
        SqlExecutions sqlExecutions = new SqlExecutions();
        sqlExecutions.add(anExecution(), anInsert());
        sqlExecutions.add(anExecution(), anInsert());
        executionCountOf(sqlExecutions).set(1);

        // WHEN
        SqlExecutions deserializedSqlExecutions = serializeAndDeserialize(sqlExecutions);

        // THEN
        assertThat(deserializedSqlExecutions).hasSize(2);
        assertThat(deserializedSqlExecutions.getNumberOfExecutions()).isEqualTo(2);

    }

    private static ExecutionInfo anExecution() {
        ExecutionInfo executionInfo = new ExecutionInfo();
        executionInfo.setDataSourceName("dataSource");
        executionInfo.setConnectionId("1");
        executionInfo.setStatementType(StatementType.PREPARED);
        return executionInfo;
    }

    private static List<QueryInfo> anInsert() {
        return Collections.singletonList(new QueryInfo("INSERT INTO Book(title) VALUES ('title')"));
    }

    private static AtomicInteger executionCountOf(SqlExecutions sqlExecutions) throws Exception {
        Field executionCount = SqlExecutions.class.getDeclaredField("executionCount");
        executionCount.setAccessible(true);
        return (AtomicInteger) executionCount.get(sqlExecutions);
    }

    private static SqlExecutions serializeAndDeserialize(SqlExecutions sqlExecutions) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bytes);
        out.writeObject(sqlExecutions);
        out.close();
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
        try {
            return (SqlExecutions) in.readObject();
        } finally {
            in.close();
        }
    }

}
