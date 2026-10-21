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
package org.quickperf.spring.springboottest;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.spring.springboottest.service.SpringDbConnectionProfiling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

class SpringBoot3ConnectionProfilingJunit5Test {

    @Test
    void should_start_the_stack_trace_of_a_data_source_call_with_the_data_source_proxy() throws IOException {

        // GIVEN
        Class<?> testClass = SpringDbConnectionProfiling.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isZero();

        String profilingResult = readContentOf(SpringDbConnectionProfiling.DB_PROFILING_FILE_PATH);

        // Spring Boot 3.2 brings Spring 6.1. From Spring 5.2 to 6.1, the CGLIB proxy of the DataSource calls the
        // QuickPerf interceptor through three Spring AOP elements instead of two.
        assertThat(profilingResult.replaceAll("connection .* -", "connection id -"))
                .startsWith("connection id - javax.sql.DataSource.getConnection()" + System.lineSeparator()
                          + "\tcom.zaxxer.hikari.HikariDataSource$$SpringCGLIB$$"
                           );

    }

    private String readContentOf(String filePath) throws IOException {
        return Files.lines(Paths.get(filePath))
                .collect(joining(System.lineSeparator()));
    }

}
