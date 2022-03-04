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
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalAnnotationJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class TestClass extends SqlTestBaseJUnit4 {

        @Test
        public void execute_one_statement_containing_a_like_with_a_leading_wildcard() {
            EntityManager em = emf.createEntityManager();
            Query nativeQuery = em.createNativeQuery("SELECT * FROM Book b WHERE b.title LIKE  '%Ja'");
            nativeQuery.getResultList();
        }

    }

    @Test public void
    should_apply_global_annotation() {

        // GIVEN
        Class<TestClass> testClass = TestClass.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Like with leading wildcard");

    }

}
