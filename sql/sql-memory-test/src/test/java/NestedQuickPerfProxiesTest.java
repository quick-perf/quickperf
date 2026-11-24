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
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.ExpectNoConnectionLeak;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.connection.Level;
import org.quickperf.writer.WriterFactory;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

public class NestedQuickPerfProxiesTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DirectlyNestedProxies {
        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_count_a_select_once_through_directly_nested_proxies() {

        // GIVEN
        Class<?> testClass = DirectlyNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DirectlyNestedProxiesWithAStatement {
        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeQuery("select 1");
            }
        }
    }

    @Test public void
    should_count_a_select_executed_with_a_statement_once_through_directly_nested_proxies() {

        // GIVEN
        Class<?> testClass = DirectlyNestedProxiesWithAStatement.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectMissedThroughNestedProxies {
        @ExpectSelect(0)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_still_record_a_select_through_nested_proxies() {

        // GIVEN
        Class<?> testClass = SelectMissedThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();
        assertThat(testResult.toString())
                .contains("You may think that <0> select statement was sent to the database")
                .contains("But there is in fact <1>...");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class DataSourceHandingOverTheConnectionBetweenProxies {
        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            // Like an AbstractRoutingDataSource or a DelegatingDataSource
            DataSource router = new ConnectionHandingOverDataSource(quickPerfProxyOf(anH2DataSource()));
            DataSource dataSource = quickPerfProxyOf(router);
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_count_a_select_once_through_a_datasource_handing_over_the_connection_between_proxies() {

        // GIVEN
        Class<?> testClass = DataSourceHandingOverTheConnectionBetweenProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionProxyBetweenProxies {
        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            // Like a LazyConnectionDataSourceProxy or a TransactionAwareDataSourceProxy
            DataSource lazyDataSource = new ConnectionProxyingDataSource(quickPerfProxyOf(anH2DataSource()));
            DataSource dataSource = quickPerfProxyOf(lazyDataSource);
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_count_a_select_once_through_a_connection_proxy_between_proxies() {

        // GIVEN
        Class<?> testClass = ConnectionProxyBetweenProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ApplicationDataSourceProxyBetweenProxies {

        static final QueryCountingListener APPLICATION_LISTENER = new QueryCountingListener();

        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            ProxyDataSource applicationProxy = new ProxyDataSource(quickPerfProxyOf(anH2DataSource()));
            applicationProxy.addListener(APPLICATION_LISTENER);
            DataSource dataSource = quickPerfProxyOf(applicationProxy);
            executeOneSelectWithAPreparedStatement(dataSource);
        }

    }

    @Test public void
    should_count_a_select_once_through_an_application_datasource_proxy_between_proxies() {

        // GIVEN
        Class<?> testClass = ApplicationDataSourceProxyBetweenProxies.class;
        ApplicationDataSourceProxyBetweenProxies.APPLICATION_LISTENER.reset();

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(ApplicationDataSourceProxyBetweenProxies.APPLICATION_LISTENER.afterQueryCount())
                .isEqualTo(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProxyOfAnApplicationDataSourceProxy {

        static final QueryCountingListener APPLICATION_LISTENER = new QueryCountingListener();

        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            ProxyDataSource applicationProxy = new ProxyDataSource(anH2DataSource());
            applicationProxy.addListener(APPLICATION_LISTENER);
            DataSource dataSource = quickPerfProxyOf(applicationProxy);
            executeOneSelectWithAPreparedStatement(dataSource);
        }

    }

    @Test public void
    should_count_a_select_through_a_proxy_of_an_application_datasource_proxy() {

        // GIVEN
        Class<?> testClass = ProxyOfAnApplicationDataSourceProxy.class;
        ProxyOfAnApplicationDataSourceProxy.APPLICATION_LISTENER.reset();

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(ProxyOfAnApplicationDataSourceProxy.APPLICATION_LISTENER.afterQueryCount())
                .isEqualTo(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ListenerAddedToTheOuterProxy {

        static final QueryCountingListener APPLICATION_LISTENER = new QueryCountingListener();

        @ExpectSelect(1)
        @Test
        public void execute_one_select() throws SQLException {
            ProxyDataSource outerProxy = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            outerProxy.addListener(APPLICATION_LISTENER);
            executeOneSelectWithAPreparedStatement(outerProxy);
        }

    }

    @Test public void
    should_send_the_queries_to_a_listener_added_to_the_outer_proxy() {

        // GIVEN
        Class<?> testClass = ListenerAddedToTheOuterProxy.class;
        ListenerAddedToTheOuterProxy.APPLICATION_LISTENER.reset();

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(ListenerAddedToTheOuterProxy.APPLICATION_LISTENER.afterQueryCount())
                .isEqualTo(1);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class IndependentProxies {
        @ExpectSelect(2)
        @Test
        public void execute_one_select_with_each_datasource() throws SQLException {
            executeOneSelectWithAPreparedStatement(quickPerfProxyOf(anH2DataSource()));
            executeOneSelectWithAPreparedStatement(quickPerfProxyOf(anH2DataSource()));
        }
    }

    @Test public void
    should_count_the_selects_of_independent_proxies() {

        // GIVEN
        Class<?> testClass = IndependentProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    private static final String GET_CONNECTION_EVENT = "javax.sql.DataSource.getConnection()";

    private static final String GET_CONNECTION_WITH_USER_NAME_EVENT = "javax.sql.DataSource.getConnection(String username, String password)";

    private static String profilingFilePath(String fileName) {
        return Paths.get("target").toFile().getAbsolutePath() + File.separator + fileName;
    }

    public abstract static class ProfilingFileWriter implements WriterFactory {

        private final String filePath;

        ProfilingFileWriter(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(filePath);
        }

    }

    public static class DirectNestingProfilingWriter extends ProfilingFileWriter {
        static final String FILE_PATH = profilingFilePath("nested-proxies-direct-nesting-profiling.txt");
        public DirectNestingProfilingWriter() {
            super(FILE_PATH);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionThroughNestedProxies {
        @ProfileConnection(level = Level.TRACE, writerFactory = DirectNestingProfilingWriter.class)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_profile_connection_events_once_through_nested_proxies() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(DirectNestingProfilingWriter.FILE_PATH, GET_CONNECTION_EVENT);

    }

    public static class UserNameProfilingWriter extends ProfilingFileWriter {
        static final String FILE_PATH = profilingFilePath("nested-proxies-user-name-profiling.txt");
        public UserNameProfilingWriter() {
            super(FILE_PATH);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithUserNameThroughNestedProxies {
        @ProfileConnection(level = Level.TRACE, writerFactory = UserNameProfilingWriter.class)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            try (Connection connection = dataSource.getConnection(H2_USER_NAME, H2_PASSWORD);
                 PreparedStatement statement = connection.prepareStatement("select 1")) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_profile_connection_events_once_through_nested_proxies_when_getting_the_connection_with_a_user_name() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionWithUserNameThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(UserNameProfilingWriter.FILE_PATH, GET_CONNECTION_WITH_USER_NAME_EVENT);

    }

    public static class ApplicationProxyBetweenProxiesProfilingWriter extends ProfilingFileWriter {
        static final String FILE_PATH = profilingFilePath("nested-proxies-application-proxy-between-profiling.txt");
        public ApplicationProxyBetweenProxiesProfilingWriter() {
            super(FILE_PATH);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionThroughAnApplicationDataSourceProxyBetweenProxies {
        @ProfileConnection(level = Level.TRACE, writerFactory = ApplicationProxyBetweenProxiesProfilingWriter.class)
        @Test
        public void execute_one_select() throws SQLException {
            ProxyDataSource applicationProxy = new ProxyDataSource(quickPerfProxyOf(anH2DataSource()));
            DataSource dataSource = quickPerfProxyOf(applicationProxy);
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_profile_connection_events_once_through_an_application_datasource_proxy_between_proxies() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionThroughAnApplicationDataSourceProxyBetweenProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(ApplicationProxyBetweenProxiesProfilingWriter.FILE_PATH, GET_CONNECTION_EVENT);

    }

    public static class ProxyOfAnApplicationProxyProfilingWriter extends ProfilingFileWriter {
        static final String FILE_PATH = profilingFilePath("nested-proxies-proxy-of-an-application-proxy-profiling.txt");
        public ProxyOfAnApplicationProxyProfilingWriter() {
            super(FILE_PATH);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionThroughAProxyOfAnApplicationDataSourceProxy {
        @ProfileConnection(level = Level.TRACE, writerFactory = ProxyOfAnApplicationProxyProfilingWriter.class)
        @Test
        public void execute_one_select() throws SQLException {
            ProxyDataSource applicationProxy = new ProxyDataSource(anH2DataSource());
            DataSource dataSource = quickPerfProxyOf(applicationProxy);
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_profile_connection_events_through_a_proxy_of_an_application_datasource_proxy() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionThroughAProxyOfAnApplicationDataSourceProxy.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThatTheEventsOfOneSelectAreProfiledOnce(ProxyOfAnApplicationProxyProfilingWriter.FILE_PATH, GET_CONNECTION_EVENT);

    }

    private static void assertThatTheEventsOfOneSelectAreProfiledOnce(String profilingFilePath, String getConnectionEvent) throws IOException {
        List<String> profilingLines = Files.readAllLines(Paths.get(profilingFilePath));
        assertThat(profilingLines).hasSize(3);
        String connectionId = connectionIdOf(profilingLines.get(0));
        assertThat(profilingLines).containsExactly(
                connectionId + " - " + getConnectionEvent
              , connectionId + " - java.sql.Connection.prepareStatement(String sql) [sql: select 1]"
              , connectionId + " - java.sql.Connection.close()");
    }

    private static String connectionIdOf(String profilingLine) {
        return profilingLine.substring(0, profilingLine.indexOf(" - "));
    }

    public static class StackTraceProfilingWriter extends ProfilingFileWriter {
        static final String FILE_PATH = profilingFilePath("nested-proxies-stack-trace-profiling.txt");
        public StackTraceProfilingWriter() {
            super(FILE_PATH);
        }
    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ProfileConnectionWithStackTraceThroughNestedProxies {
        @ProfileConnection(level = Level.TRACE, displayStackTrace = true, writerFactory = StackTraceProfilingWriter.class)
        @Test
        public void execute_one_select() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement("select 1")) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_display_the_stack_traces_from_the_test_method_through_nested_proxies() throws IOException {

        // GIVEN
        Class<?> testClass = ProfileConnectionWithStackTraceThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

        List<String> profilingLines = Files.readAllLines(Paths.get(StackTraceProfilingWriter.FILE_PATH));
        List<String> firstStackTraceElementOfEachEvent = new ArrayList<>();
        for (int i = 0; i < profilingLines.size(); i++) {
            boolean isAnEventLine = !profilingLines.get(i).startsWith("\t");
            if (isAnEventLine) {
                String nextLine = i + 1 < profilingLines.size() ? profilingLines.get(i + 1) : "";
                firstStackTraceElementOfEachEvent.add(nextLine.replaceAll("java:\\d+\\)", "java:lineNumber)"));
            }
        }
        String testMethodElement = "\tNestedQuickPerfProxiesTest$ProfileConnectionWithStackTraceThroughNestedProxies"
                                 + ".execute_one_select(NestedQuickPerfProxiesTest.java:lineNumber)";
        assertThat(firstStackTraceElementOfEachEvent).containsExactly(testMethodElement, testMethodElement, testMethodElement);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class ConnectionLeakThroughNestedProxies {
        @ExpectNoConnectionLeak
        @Test
        public void execute_one_select_without_closing_the_connection() throws SQLException {
            DataSource dataSource = quickPerfProxyOf(quickPerfProxyOf(anH2DataSource()));
            Connection connection = dataSource.getConnection();
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                statement.executeQuery();
            }
        }
    }

    @Test public void
    should_detect_a_connection_leak_through_nested_proxies() {

        // GIVEN
        Class<?> testClass = ConnectionLeakThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();
        assertThat(testResult.toString()).contains("Database connection leak");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class NoConnectionLeakThroughNestedProxies {
        @ExpectNoConnectionLeak
        @Test
        public void execute_one_select() throws SQLException {
            DataSource router = new ConnectionHandingOverDataSource(quickPerfProxyOf(anH2DataSource()));
            DataSource dataSource = quickPerfProxyOf(router);
            executeOneSelectWithAPreparedStatement(dataSource);
        }
    }

    @Test public void
    should_not_detect_a_connection_leak_through_nested_proxies_if_the_connection_is_closed() {

        // GIVEN
        Class<?> testClass = NoConnectionLeakThroughNestedProxies.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();

    }

    private static final String H2_USER_NAME = "qp";

    private static final String H2_PASSWORD = "";

    private static final AtomicInteger DATABASE_COUNTER = new AtomicInteger();

    private static DataSource anH2DataSource() {
        JdbcDataSource h2DataSource = new JdbcDataSource();
        h2DataSource.setURL("jdbc:h2:mem:nested-proxies-" + DATABASE_COUNTER.incrementAndGet());
        h2DataSource.setUser(H2_USER_NAME);
        h2DataSource.setPassword(H2_PASSWORD);
        return h2DataSource;
    }

    private static ProxyDataSource quickPerfProxyOf(DataSource dataSource) {
        return aDataSourceBuilder().buildProxy(dataSource);
    }

    private static void executeOneSelectWithAPreparedStatement(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select 1")) {
            statement.executeQuery();
        }
    }

    public static class QueryCountingListener implements QueryExecutionListener {

        private final AtomicInteger afterQueryCount = new AtomicInteger();

        @Override
        public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) { }

        @Override
        public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) {
            afterQueryCount.incrementAndGet();
        }

        int afterQueryCount() {
            return afterQueryCount.get();
        }

        void reset() {
            afterQueryCount.set(0);
        }

    }

    private abstract static class DelegatingDataSource implements DataSource {

        final DataSource target;

        DelegatingDataSource(DataSource target) {
            this.target = target;
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return target.getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            target.setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            target.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return target.getLoginTimeout();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return target.getParentLogger();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return target.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return target.isWrapperFor(iface);
        }

    }

    private static class ConnectionHandingOverDataSource extends DelegatingDataSource {

        ConnectionHandingOverDataSource(DataSource target) {
            super(target);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return target.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return target.getConnection(username, password);
        }

    }

    // Returns its own connection proxy, which hands over the statements of the target connection
    private static class ConnectionProxyingDataSource extends DelegatingDataSource {

        ConnectionProxyingDataSource(DataSource target) {
            super(target);
        }

        @Override
        public Connection getConnection() throws SQLException {
            return proxyOf(target.getConnection());
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return proxyOf(target.getConnection(username, password));
        }

        private static Connection proxyOf(final Connection targetConnection) {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader()
                  , new Class<?>[]{Connection.class}
                  , (proxy, method, args) -> {
                        try {
                            return method.invoke(targetConnection, args);
                        } catch (InvocationTargetException e) {
                            throw e.getTargetException();
                        }
                    });
        }

    }

}
