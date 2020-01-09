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
 * Copyright 2020-2020 the original author or authors.
 */

package org.quickperf.micronaut.micronauttest;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.micronaut.micronauttest.controller.DetectionOfNPlusOneSelectInWebService;
import org.quickperf.micronaut.micronauttest.service.DetectionOfNPlusOneSelectInService;

import static org.assertj.core.api.Assertions.assertThat;

public class MicronautTest {

    @Test
    public void should_fail_if_select_number_is_greater_than_expected_from_web_service_and_with_test_launched_in_a_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectInWebService.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(1);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <1> select statement was sent to the database")
                .contains("Perhaps you are facing a N+1 select issue");

    }

    @Test
    public void should_fail_if_select_number_is_greater_than_expected_from_spring_service() {


        // GIVEN
        Class<?> testClass = DetectionOfNPlusOneSelectInService.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isEqualTo(1);

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("You may think that <1> select statement was sent to the database")
                .contains("Perhaps you are facing a N+1 select issue");

    }

}
