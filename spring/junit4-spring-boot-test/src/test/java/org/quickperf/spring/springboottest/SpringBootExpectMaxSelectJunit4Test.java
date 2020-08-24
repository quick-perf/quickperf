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

package org.quickperf.spring.springboottest;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.quickperf.spring.springboottest.service.DetectionOfNPlusOneSelectInServiceWithExpectMaxSelect;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootExpectMaxSelectJunit4Test {

    @Test
    public void should_fail_if_select_number_is_greater_than_expected() {

        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectInServiceWithExpectMaxSelect.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        String testReport = printableResult.toString();
        assertThat(testReport)
                .contains("You may think that at most <1> select statement was sent to the database")
                .contains("But in fact <3>...")
                .contains("Perhaps you are facing an N+1 select issue")
                .contains("With Hibernate, you may fix it by")
                .contains("With Spring Data JPA, you may fix it by");

    }

}
