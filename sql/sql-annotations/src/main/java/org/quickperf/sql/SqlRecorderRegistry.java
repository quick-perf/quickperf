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

package org.quickperf.sql;

import java.util.ArrayList;
import java.util.Collection;

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

public class SqlRecorderRegistry {

    public static final SqlRecorderRegistry INSTANCE = new SqlRecorderRegistry();

    private final Collection<SqlRecorder> sqlRecordersOfTestJvm = new ArrayList<>();

    private static final InheritableThreadLocal<Collection<SqlRecorder>> SQL_RECORDERS_WHEN_ONE_JVM = new InheritableThreadLocal<Collection<SqlRecorder>>() {
        @Override
        protected Collection<SqlRecorder> initialValue() {
            return new ArrayList<>();
        }
    };

    private SqlRecorderRegistry() {}

    public void register(SqlRecorder sqlRecorder) {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            sqlRecordersOfTestJvm.add(sqlRecorder);
        } else {
            Collection<SqlRecorder> sqlRecorders = SQL_RECORDERS_WHEN_ONE_JVM.get();
            sqlRecorders.add(sqlRecorder);
        }
    }

    public static void unregister(SqlRecorder sqlRecorder) {
        if(!TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Collection<SqlRecorder> sqlRecorders = SQL_RECORDERS_WHEN_ONE_JVM.get();
            sqlRecorders.remove(sqlRecorder);
        }
    }

    public Collection<SqlRecorder> getSqlRecorders() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return sqlRecordersOfTestJvm;
        }
        return SQL_RECORDERS_WHEN_ONE_JVM.get();
    }

}
