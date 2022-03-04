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

import org.junit.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlRecorderRegistryTest {

    @Test public void
    should_get_a_sql_recorder_from_its_type() {

        // GIVEN
        SqlRecorder registeredSqlRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(registeredSqlRecorder);

        // WHEN
        SqlRecorder retrievedSqlRecorder = SqlRecorderRegistry.INSTANCE
                                          .getSqlRecorderOfType(PersistenceSqlRecorder.class);

        // THEN
        assertThat(retrievedSqlRecorder).isEqualTo(registeredSqlRecorder);

    }

    @Test public void
    should_clear_sql_recorder_registry() {

        // GIVEN
        SqlRecorder registeredSqlRecorder = new PersistenceSqlRecorder();
        SqlRecorderRegistry.INSTANCE.register(registeredSqlRecorder);

        // WHEN
        SqlRecorderRegistry.INSTANCE.clear();

        // THEN
        Collection<SqlRecorder> sqlRecorders = SqlRecorderRegistry.INSTANCE.getSqlRecorders();
        assertThat(sqlRecorders).hasSize(0);

    }

}