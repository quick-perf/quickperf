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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.ExpectSelect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExecuteMethodOnStatementTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ExecuteOnStatement extends SqlTestBase {

        @Test
        @ExpectSelect
        public void call_execute_method_on_statement() throws SQLException {
            try(Connection connection = getConnection();
                Statement statement = connection.createStatement()) {
                statement.execute("select isbn from Book");
            }
        }

    }

    @Test public void
    should_not_fail_with_execute_method_on_statement() {

        // GIVEN
        Class<?> testClass = ExecuteOnStatement.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }


}
