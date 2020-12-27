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
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ConnectionLeakTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionLeak extends SqlTestBase {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
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
    public static class NoConnectionLeak extends SqlTestBase {
        @ExpectNoConnectionLeak
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
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
    public static class ConnectionLeakInNewJvm extends SqlTestBase {

        @ExpectNoConnectionLeak
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test() throws SQLException {
            Connection connection = getConnection();
            try (PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
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
    public static class NoConnectionLeakInNewJvm extends SqlTestBase {
        @ExpectNoConnectionLeak
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test() throws SQLException {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement("select isbn from Book");) {
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

}
