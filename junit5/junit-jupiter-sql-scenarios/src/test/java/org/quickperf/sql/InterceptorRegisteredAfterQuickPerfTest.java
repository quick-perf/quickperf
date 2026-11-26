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
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.config.MemoryDataSourceBuilder;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// QuickPerf executes the test method through the JUnit invocation, and not by reflection: the interceptors registered
// after QuickPerf are executed, inside the QuickPerf recording
class InterceptorRegisteredAfterQuickPerfTest {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    static class InterceptorExecutingASelect implements InvocationInterceptor {

        static final AtomicInteger INTERCEPTIONS = new AtomicInteger();

        @Override
        public void interceptTestMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
            INTERCEPTIONS.incrementAndGet();
            executeASelect();
            invocation.proceed();
        }

    }

    @QuickPerfUnderTest
    @ExtendWith(InterceptorExecutingASelect.class)
    static class TestWithAnInterceptorRegisteredAfterQuickPerf {

        @ExpectSelect(2)
        @Test
        public void execute_one_select() {
            executeASelect();
        }

    }

    @Test
    public void should_execute_and_measure_the_interceptors_registered_after_quickperf() {

        // GIVEN
        int interceptionsBefore = InterceptorExecutingASelect.INTERCEPTIONS.get();

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestWithAnInterceptorRegisteredAfterQuickPerf.class).run();

        // THEN
        assertThat(result.getNumberOfFailures())
                .as("The select of the interceptor should be measured with the select of the test method.%s"
                  , result.getErrorReport())
                .isZero();
        assertThat(InterceptorExecutingASelect.INTERCEPTIONS.get() - interceptionsBefore)
                .as("interceptions of the test method")
                .isOne();

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
