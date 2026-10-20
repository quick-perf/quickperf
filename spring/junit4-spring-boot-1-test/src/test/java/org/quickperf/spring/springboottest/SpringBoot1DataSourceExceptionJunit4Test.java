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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.quickperf.spring.springboottest.datasourceexception.SqlExceptionOfDataSource;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBoot1DataSourceExceptionJunit4Test {

    @Test
    public void should_fail_with_the_sql_exception_of_a_datasource_bean_refusing_the_connection() {

        // GIVEN
        Class<?> testClass = SqlExceptionOfDataSource.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testReport = printableResult.toString();
        // The SQLException starts a line: it is the exception of the failure, not the cause of another one.
        assertThat(testReport)
                      .contains(System.lineSeparator() + "java.sql.SQLException: Connection refused")
                      .doesNotContain("UndeclaredThrowableException")
                      .doesNotContain("InvocationTargetException");

    }

}
