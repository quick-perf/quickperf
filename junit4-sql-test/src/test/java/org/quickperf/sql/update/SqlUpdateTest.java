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

package org.quickperf.sql.update;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;

public class SqlUpdateTest {

    @Test
    public void
    should_fail_if_the_number_of_update_requests_is_not_equal_to_the_number_expected() {

        // GIVEN
        Class<?> testClass = SqlUpdate.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.failureCount()).isEqualTo(1);
        softAssertions.assertThat(printableResult.toString()).contains("Expected number of UPDATE requests <5> but is <1>");
        softAssertions.assertThat(printableResult.toString()).contains("update")
                .contains("Book")
                .contains("set")
                .contains("isbn=?");
        softAssertions.assertAll();

    }

}
