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
package org.quickperf.spring.springboottest.jooqtest;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@QuickPerfTest
@JooqTest
@Sql(statements = {
        "CREATE TABLE IF NOT EXISTS PLAYER_JOOQ_TEST (id BIGINT PRIMARY KEY, name VARCHAR(255))",
        "INSERT INTO PLAYER_JOOQ_TEST VALUES (1, 'Paul Pogba')",
        "INSERT INTO PLAYER_JOOQ_TEST VALUES (2, 'Antoine Griezmann')"
})
public class ExpectSelectWithJooq {

    @Autowired
    private DSLContext dslContext;

    @ExpectSelect(1)
    @Test
    public void execute_two_selects() {

        Result<?> player1 =
                dslContext.fetch("SELECT id, name FROM PLAYER_JOOQ_TEST WHERE id = 1");

        Result<?> player2 =
                dslContext.fetch("SELECT id, name FROM PLAYER_JOOQ_TEST WHERE id = 2");

        assertThat(player1).hasSize(1);
        assertThat(player2).hasSize(1);

    }

}
