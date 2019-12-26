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
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlTestBaseJUnit4;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class GlobalAnnotationJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlCrossJoin extends SqlTestBaseJUnit4 {

        @Test
        public void execute_one_cross_join() {
            EntityManager entityManager = emf.createEntityManager();
            String nativeQuery = "SELECT b1.* FROM Book b1 CROSS JOIN Book b2";
            Query query = entityManager.createNativeQuery(nativeQuery);
            query.getResultList();
        }

    }

    @Test public void
    should_apply_global_annotation() {

        // GIVEN
        Class<SqlCrossJoin> testClass = SqlCrossJoin.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(printableResult.failureCount())
                      .isEqualTo(1);

        softAssertions.assertThat(printableResult.toString())
                      .contains("cross join detected")
                      .contains("CROSS JOIN") //query cross join
        ;

        softAssertions.assertAll();

    }

}
