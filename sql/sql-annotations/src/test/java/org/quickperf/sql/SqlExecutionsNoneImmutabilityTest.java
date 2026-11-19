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

import org.junit.Test;
import org.quickperf.issue.PerfIssue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Defends the JVM-wide-shared {@link SqlExecutions#NONE} singleton against
 * cross-test contamination tagging. NONE is observed by every test that does
 * not produce SQL; tagging it would fail every test that subsequently reads
 * it.
 */
public class SqlExecutionsNoneImmutabilityTest {

    @Test public void
    marking_NONE_as_contaminated_should_be_a_no_op() {
        SqlExecutions.NONE.markCrossTestContamination();

        assertThat(SqlExecutions.NONE.hasCrossTestContamination()).isFalse();
    }

    @Test public void
    NONE_should_never_report_a_cross_test_contamination_issue() {
        SqlExecutions.NONE.markCrossTestContamination();

        assertThat(SqlExecutions.NONE.verifyRecord()).isSameAs(PerfIssue.NONE);
    }

}
