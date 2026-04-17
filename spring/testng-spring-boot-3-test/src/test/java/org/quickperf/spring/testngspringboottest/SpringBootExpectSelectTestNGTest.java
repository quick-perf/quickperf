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
package org.quickperf.spring.testngspringboottest;

import org.quickperf.spring.testngspringboottest.service.DetectionOfNPlusOneSelectInServiceWithExpectSelect;
import org.quickperf.testng.TestNGTests;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpringBootExpectSelectTestNGTest {

    @Test
    public void should_fail_if_select_number_is_greater_than_expected_from_spring_service() {

        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectInServiceWithExpectSelect.class;
        TestNGTests testNGTests = TestNGTests.createInstance(testClass);

        // WHEN
        TestNGTests.TestNGTestsResult testsResult = testNGTests.run();

        // THEN
        assertThat(testsResult.getNumberOfFailedTest()).isOne();

        Throwable errorReport = testsResult.getThrowableOfFirstTest();
        assertThat(errorReport)
                      .hasMessageContaining("You may think that <1> select statement was sent to the database")
                      .hasMessageContaining("Perhaps you are facing an N+1 select issue")
                      .hasMessageContaining("With Hibernate, you may fix it by")
                      .hasMessageContaining("With Spring Data JPA, you may fix it by");

    }

}
