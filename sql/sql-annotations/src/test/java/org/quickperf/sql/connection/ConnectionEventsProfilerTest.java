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

package org.quickperf.sql.connection;

import org.junit.Test;
import org.mockito.Mockito;
import org.quickperf.sql.connection.stack.StackTraceDisplayConfig;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.*;

public class ConnectionEventsProfilerTest {

    @Test public void
    should_profile_a_connection_method_with_no_argument() {

        // GIVEN
        StringWriter stringWriter = new StringWriter();
        ConnectionEventsProfiler connectionEventsProfiler
                = buildAConnectionEventsProfiler(stringWriter);
        connectionEventsProfiler.start();

        // WHEN
        Connection aConnection = Mockito.mock(Connection.class);
        connectionEventsProfiler.close(aConnection);

        // THEN
        String outputAsString = stringWriter.toString();
        String expectedResult = "java.sql.Connection.close()" + System.lineSeparator();
        assertThat(outputAsString).endsWith(expectedResult);

    }

    @Test public void
    should_profile_a_connection_method_with_one_argument() {

        // GIVEN
        StringWriter stringWriter = new StringWriter();
        ConnectionEventsProfiler connectionEventsProfiler
                = buildAConnectionEventsProfiler(stringWriter);
        connectionEventsProfiler.start();

        // WHEN
        Connection aConnection = Mockito.mock(Connection.class);
        connectionEventsProfiler.prepareStatement(aConnection, "SELECT col FROM table");

        // THEN
        String outputAsString = stringWriter.toString();
        String expectedResult = "prepareStatement(String sql)"
                              + " [sql: " +  "SELECT col FROM table" + "]"
                              + System.lineSeparator();
        assertThat(outputAsString).endsWith(expectedResult);

    }

    private ConnectionEventsProfiler buildAConnectionEventsProfiler(StringWriter stringWriter) {
        Level aLevel = Level.TRACE;
        StackTraceDisplayConfig aStacktraceDisplayConfig = StackTraceDisplayConfig.noStackTrace();
        PrintWriter writer = new PrintWriter(stringWriter);
        ProfilingParameters profilingParameters = new ProfilingParameters(aLevel, aStacktraceDisplayConfig, writer);
        ConnectionEventsProfiler connectionEventsProfiler = new ConnectionEventsProfiler(profilingParameters);
        return connectionEventsProfiler;
    }

    @Test public void
    should_profile_a_connection_method_with_several_arguments() {

        // GIVEN
        StringWriter stringWriter = new StringWriter();
        ConnectionEventsProfiler connectionEventsProfiler
                = buildAConnectionEventsProfiler(stringWriter);
        connectionEventsProfiler.start();

        // WHEN
        Connection aConnection = Mockito.mock(Connection.class);
        connectionEventsProfiler.createStatement( aConnection, ResultSet.TYPE_FORWARD_ONLY
                                                , ResultSet.CONCUR_READ_ONLY);

        // THEN
        String outputAsString = stringWriter.toString();
        String expectedResult
                = "createStatement(Connection connection, int resultSetType, int resultSetConcurrency) [resultSetType: "
                + ResultSet.TYPE_FORWARD_ONLY + ", resultSetConcurrency: " + ResultSet.CONCUR_READ_ONLY + "]"
                + System.lineSeparator();
        assertThat(outputAsString).endsWith(expectedResult);

    }

}