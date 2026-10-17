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
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

public class ConnectionLeakTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionLeak {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            Connection connection = buildDataSource().getConnection();
            try (PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_detect_a_connection_leak() {

        // GIVEN
        Class<?> testClass = ConnectionLeak.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        String testReport = testResult.toString();
        assertThat(testReport).contains("Database connection leak");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoConnectionLeak {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            try (Connection connection = buildDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_not_fail_if_no_connection_leak() {

        // GIVEN
        Class<?> testClass = NoConnectionLeak.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }


    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionLeakWithUserNameAndPassword {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            Connection connection = buildDataSource().getConnection("qp", "");
            try (PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_detect_a_connection_leak_with_a_connection_got_from_user_name_and_password() {

        // GIVEN
        Class<?> testClass = ConnectionLeakWithUserNameAndPassword.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        String testReport = testResult.toString();
        assertThat(testReport).contains("Database connection leak");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoConnectionLeakWithUserNameAndPassword {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            try (Connection connection = buildDataSource().getConnection("qp", "");
                 PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_not_fail_if_no_connection_leak_with_a_connection_got_from_user_name_and_password() {

        // GIVEN
        Class<?> testClass = NoConnectionLeakWithUserNameAndPassword.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionLeakInNewJvm {

        @ExpectNoConnectionLeak
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test() throws SQLException {
            Connection connection = buildDataSource().getConnection();
            try (PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_detect_a_connection_leak_in_new_jvm() {

        // GIVEN
        Class<?> testClass = ConnectionLeakInNewJvm.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        String testReport = testResult.toString();
        assertThat(testReport).contains("Database connection leak");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoConnectionLeakInNewJvm {
        @ExpectNoConnectionLeak
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test() throws SQLException {
            try (Connection connection = buildDataSource().getConnection();
                 PreparedStatement statement = connection.prepareStatement("select 1");) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_not_fail_if_no_connection_leak_in_new_jvm() {

        // GIVEN
        Class<?> testClass = NoConnectionLeakInNewJvm.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    private static DataSource buildDataSource() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:connection-leak-test");
        h2DataSource.setUser("qp");
        h2DataSource.setPassword("");
        return aDataSourceBuilder().buildProxy(h2DataSource);
    }

}
