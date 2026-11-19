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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlRecorderRegistry;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.sql.connection.ConnectionEventsProfiler;
import org.quickperf.sql.connection.ConnectionListenerRegistry;
import org.quickperf.sql.display.DisplaySqlRecorder;
import org.quickperf.writer.WriterFactory;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// Under a Timeout rule, JUnit executes the @Before methods, the test method
// and the @After methods on a thread started for the test, while QuickPerf
// builds the test execution context on the runner thread.
public class TestMethodOnTimeoutThreadTest {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    // Under a Timeout rule, QuickPerf cleans the test resources on the rule's
    // thread, so the recorders registered on this thread when building the
    // test execution context stay registered here, and would display the SQL
    // of the next tests executed by this thread.
    @After
    public void unregister_the_recorders_left_on_this_thread() {
        SqlRecorderRegistry.INSTANCE.clear();
        ConnectionListenerRegistry.INSTANCE.clear();
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DisplaySqlWithATimeoutRule {

        @Rule
        public Timeout timeout = Timeout.seconds(20);

        @Before
        public void before() {
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                    .as("recorder displaying the SQL of the thread executing @Before")
                    .isNotNull();
        }

        @DisplaySql
        @ExpectSelect(1)
        @Test
        public void execute_a_select() {
            executeASelect();
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                    .as("recorder displaying the SQL of the thread executing the test method")
                    .isNotNull();
        }

        @After
        public void after() {
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                    .as("recorder displaying the SQL of the thread executing @After")
                    .isNotNull();
        }

    }

    @Test public void
    should_display_the_sql_of_a_test_executed_on_the_thread_of_a_timeout_rule() {

        // WHEN
        PrintableResult printableResult = testResult(DisplaySqlWithATimeoutRule.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithATimeoutRule {

        @Rule
        public Timeout timeout = Timeout.seconds(20);

        @Before
        public void before() {
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .as("profiler of the connections of the thread executing @Before")
                    .isNotNull();
        }

        @ProfileConnection(beforeAndAfterTestMethodExecution = true, writerFactory = DiscardingWriterFactory.class)
        @ExpectNoConnectionLeak
        @Test
        public void execute_a_select() {
            executeASelect();
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .as("profiler of the connections of the thread executing the test method")
                    .isNotNull();
        }

        @After
        public void after() {
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .as("profiler of the connections of the thread executing @After")
                    .isNotNull();
        }

    }

    public static class DiscardingWriterFactory implements WriterFactory {
        @Override
        public Writer buildWriter() {
            return new StringWriter();
        }
    }

    @Test public void
    should_profile_the_connections_of_a_test_executed_on_the_thread_of_a_timeout_rule() {

        // WHEN
        PrintableResult printableResult = testResult(ProfileConnectionWithATimeoutRule.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    // Under @Test(timeout = ...), JUnit executes the test method on a thread
    // started for it, but not the @Before and @After methods
    @RunWith(QuickPerfJUnitRunner.class)
    public static class DisplaySqlWithATestTimeout {

        @DisplaySql
        @ExpectSelect(1)
        @Test(timeout = 20_000)
        public void execute_a_select() {
            executeASelect();
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                    .as("recorder displaying the SQL of the thread executing the test method")
                    .isNotNull();
        }

    }

    @Test public void
    should_display_the_sql_of_a_test_method_with_a_timeout() {

        // WHEN
        PrintableResult printableResult = testResult(DisplaySqlWithATestTimeout.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithATestTimeout {

        @ProfileConnection(writerFactory = DiscardingWriterFactory.class)
        @ExpectNoConnectionLeak
        @Test(timeout = 20_000)
        public void execute_a_select() {
            executeASelect();
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .as("profiler of the connections of the thread executing the test method")
                    .isNotNull();
        }

    }

    @Test public void
    should_profile_the_connections_of_a_test_method_with_a_timeout() {

        // WHEN
        PrintableResult printableResult = testResult(ProfileConnectionWithATestTimeout.class);

        // THEN
        assertThat(printableResult.failureCount()).as(printableResult.toString()).isZero();

    }

    private static void executeASelect() {
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("select 1").close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
