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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.sql.join;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableAnnotationTest {

    @Test public void
    an_annotation_should_disable_another_one() {

        // GIVEN
        Class<?> testClass = AClassHavingAMethodAnnotatedWithEnableCrossJoin.class;
        // @DisableCrossJoin is applied to each test (see QuickPerfConfiguration class).

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(0);
        softAssertions.assertAll();

    }

}
