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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.EnableLikeWithLeadingWildcard;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class DisableAnnotationTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TestClass extends SqlTestBaseJUnit4 {

        @EnableLikeWithLeadingWildcard
        @Test
        public void execute_select_who_started_with_like_wildcard() {

            EntityManager em = emf.createEntityManager();

            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE  '%Ja'");

            nativeQuery.getResultList();

        }

    }

    @Test public void
    an_annotation_can_disable_another_one() {

        // GIVEN
        Class<?> testClass = TestClass.class;
        // @DisableLikeWithLeadingWildcard is applied on each test (see QuickPerfConfiguration class).

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isZero();

    }

}
