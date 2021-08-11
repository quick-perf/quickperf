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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.quickperf.SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM;

public class SqlRecorderRegistry {

    public static final SqlRecorderRegistry INSTANCE = new SqlRecorderRegistry();

    private final Map<Class<? extends SqlRecorder>, SqlRecorder> sqlRecorderByTypeOfTestJvm = new HashMap<>();

    private static final ThreadLocal<Map<Class<? extends SqlRecorder>, SqlRecorder>> SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM
            = new InheritableThreadLocal<Map<Class<? extends SqlRecorder>, SqlRecorder>>() {
        @Override
        protected Map<Class<? extends SqlRecorder>, SqlRecorder> initialValue() {
            return new HashMap<>();
        }
    };

    private SqlRecorderRegistry() {}

    public void register(SqlRecorder sqlRecorder) {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Class<? extends SqlRecorder> sqlRecorderClass = sqlRecorder.getClass();
            sqlRecorderByTypeOfTestJvm.put(sqlRecorderClass, sqlRecorder);
        } else {
            Map<Class<? extends SqlRecorder>, SqlRecorder> sqlRecordersBytType = SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM.get();
            sqlRecordersBytType.put(sqlRecorder.getClass(), sqlRecorder);
        }
    }

    public static void unregister(SqlRecorder sqlRecorder) {
        if(!TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            Map<Class<? extends SqlRecorder>, SqlRecorder> sqlRecordersByType = SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM.get();
            sqlRecordersByType.remove(sqlRecorder);
        }
    }

    public Collection<SqlRecorder> getSqlRecorders() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            return sqlRecorderByTypeOfTestJvm.values();
        }
        Map<Class<? extends SqlRecorder>, SqlRecorder> sqlRecorderByType = SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM.get();
        return sqlRecorderByType.values();
    }

    public <T extends SqlRecorder> T getSqlRecorderOfType(Class<T> type) {
        Map<Class<? extends SqlRecorder>, SqlRecorder> sqlRecorderByType
                = SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM.get();
        return type.cast(sqlRecorderByType.get(type));
    }

    public void clear() {
        if(TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
             sqlRecorderByTypeOfTestJvm.clear();
        }
        SQL_RECORDER_BY_TYPE_WHEN_ONE_JVM.remove();
    }

}
