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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.annotation.DisableGlobalAnnotations;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.batch.SqlStatementBatchRecorder;
import org.quickperf.sql.config.MemoryDataSourceBuilder;
import org.quickperf.sql.connection.ConnectionEventsProfiler;
import org.quickperf.sql.connection.ConnectionListenerRegistry;
import org.quickperf.sql.display.DisplaySqlRecorder;
import org.quickperf.writer.WriterFactory;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// With @Timeout(threadMode = SEPARATE_THREAD) (JUnit 5.9+), JUnit executes the
// test method on a thread started for it, around the QuickPerf interceptor,
// while QuickPerf builds the test execution context in beforeEach, on the test
// thread. TestMethodOnAnotherThread does the same with the JUnit version of
// this module. It must be declared before @QuickPerfUnderTest to be executed first.
public class TestMethodOnAnotherThreadJUnit5Test {

    private static final String TEST_METHOD_THREAD = "test-method-thread";

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    public static class TestMethodOnAnotherThread implements InvocationInterceptor {

        @Override
        public void interceptTestMethod( Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
            AtomicReference<Throwable> testMethodThrowable = new AtomicReference<>();
            Thread testMethodThread = new Thread(() -> {
                try {
                    invocation.proceed();
                } catch (Throwable throwable) {
                    testMethodThrowable.set(throwable);
                }
            }, TEST_METHOD_THREAD);
            testMethodThread.start();
            testMethodThread.join();
            if (testMethodThrowable.get() != null) {
                throw testMethodThrowable.get();
            }
        }

    }

    @ExtendWith(TestMethodOnAnotherThread.class)
    @QuickPerfUnderTest
    static class DisplaySqlOnAnotherThread {

        @DisplaySql
        @ExpectSelect(1)
        @Test
        void execute_a_select() {
            assertThat(Thread.currentThread().getName()).isEqualTo(TEST_METHOD_THREAD);
            executeASelect();
            assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                    .as("recorder displaying the SQL of the thread executing the test method")
                    .isNotNull();
        }

    }

    @Test
    void should_display_the_sql_of_a_test_method_executed_on_another_thread() {

        // WHEN: on this thread, as the beforeEach callbacks
        JUnit5TestsResult result = JUnit5Tests.createInstance(DisplaySqlOnAnotherThread.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        // QuickPerf releases the recorder in afterEach, on the thread that registered it when it built the context:
        // otherwise, it would display the SQL of the next tests executed by this thread
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread that built the context")
                .isNull();

    }

    @ExtendWith(TestMethodOnAnotherThread.class)
    @QuickPerfUnderTest
    static class ProfileConnectionOnAnotherThread {

        @ProfileConnection(writerFactory = DiscardingWriterFactory.class)
        @ExpectNoConnectionLeak
        @Test
        void execute_a_select() {
            assertThat(Thread.currentThread().getName()).isEqualTo(TEST_METHOD_THREAD);
            executeASelect();
            assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                    .as("profiler of the connections of the thread executing the test method")
                    .isNotNull();
        }

    }

    public static class DiscardingWriterFactory implements WriterFactory {
        @Override
        public Writer buildWriter() {
            return new StringWriter();
        }
    }

    @Test
    void should_profile_the_connections_of_a_test_method_executed_on_another_thread() {

        // WHEN: on this thread, as the beforeEach callbacks
        JUnit5TestsResult result = JUnit5Tests.createInstance(ProfileConnectionOnAnotherThread.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(ConnectionListenerRegistry.INSTANCE.getConnectionListenerOfType(ConnectionEventsProfiler.class))
                .as("connection profiler left registered on the thread that built the context")
                .isNull();

    }

    // Without global annotations and SQL annotations, no recorder registers on
    // the thread executing the test method: QuickPerf must still claim it, so
    // that it does not take the worker-thread fallback, which returns the
    // recorders of the tests recording at the same time.
    @ExtendWith(TestMethodOnAnotherThread.class)
    @QuickPerfUnderTest
    @DisableGlobalAnnotations
    static class NoSqlAnnotationOnAnotherThread {

        @Test
        void read_the_recorders_while_another_test_records() throws Exception {
            assertThat(Thread.currentThread().getName()).isEqualTo(TEST_METHOD_THREAD);
            ExecutorService otherTestThread = Executors.newSingleThreadExecutor();
            try {
                SqlStatementBatchRecorder recorderOfOtherTest = otherTestThread.submit(() -> {
                    SqlStatementBatchRecorder recorder = new SqlStatementBatchRecorder();
                    recorder.startRecording(null);
                    return recorder;
                }).get(5, TimeUnit.SECONDS);
                try {
                    assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorders())
                            .as("recorders of the thread executing the test method")
                            .doesNotContain(recorderOfOtherTest);
                } finally {
                    otherTestThread.submit(() -> SqlRecorderRegistry.unregister(recorderOfOtherTest))
                                   .get(5, TimeUnit.SECONDS);
                }
            } finally {
                otherTestThread.shutdownNow();
            }
        }

    }

    @Test
    void should_not_give_the_recorders_of_another_test_to_a_test_method_executed_on_another_thread() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(NoSqlAnnotationOnAnotherThread.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();

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
