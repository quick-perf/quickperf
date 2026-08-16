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
import org.quickperf.spring.springboottest.datasourcedelegation.QueryCountingWithDelegatingDataSource;
import org.quickperf.spring.springboottest.datasourcedelegation.QueryCountingWithIndependentDataSources;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBoot2DataSourceDelegationJunit4Test {

    // Each test class holds one method expecting the right number of selects and one expecting a
    // wrong number. A single failure carrying the recorded number means both ran as intended.
    @Test
    public void should_count_a_select_once_when_a_datasource_bean_delegates_to_another_datasource_bean() {

        // GIVEN
        Class<?> testClass = QueryCountingWithDelegatingDataSource.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testReport = printableResult.toString();
        assertThat(testReport)
                      .contains("You may think that <2> select statements were sent to the database")
                      .contains("But there is in fact <1>");

    }

    @Test
    public void should_count_the_selects_of_each_datasource_bean_when_they_do_not_delegate() {

        // GIVEN
        Class<?> testClass = QueryCountingWithIndependentDataSources.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testReport = printableResult.toString();
        assertThat(testReport)
                      .contains("You may think that <3> select statements were sent to the database")
                      .contains("But there are in fact <2>");

    }

}
