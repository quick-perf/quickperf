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

package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.QuickPerfTest;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalAnnotationJUnit5Test {

    @QuickPerfTest
    public static class SqlCrossJoinJUnit5 extends SqlTestBaseJUnit5 {

        @Test
        public void fail_to_execute_cross_join() {
            EntityManager entityManager = emf.createEntityManager();
            String nativeQuery = "SELECT b1.* FROM Book b1 CROSS JOIN Book b2";
            Query query = entityManager.createNativeQuery(nativeQuery);
            query.getResultList();
        }

    }

    @Test
    public void should_apply_global_annotation() {

        // GIVEN
        Class<?> testClass = SqlCrossJoinJUnit5.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5Tests.JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("cross join detected")
                               .contains("CROSS JOIN") //query cross join
        ;

    }

}
