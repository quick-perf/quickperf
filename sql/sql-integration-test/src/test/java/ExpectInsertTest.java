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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectInsert;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

public class ExpectInsertTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlInsert extends SqlTestBase {

        @ExpectInsert(5)
        @Test
        public void execute_one_insert_but_five_inserts_expected() {

            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Book effectiveJava = new Book();
            effectiveJava.setIsbn("effectiveJavaIsbn");
            effectiveJava.setTitle("Effective Java");

            em.persist(effectiveJava);

            em.getTransaction().commit();

        }

    }

    @Test public void
    should_fail_if_the_number_of_insert_statements_is_not_equal_to_the_number_expected() {

        // GIVEN
        Class<?> testClass = SqlInsert.class;

        // WHEN
        PrintableResult printableResult = PrintableResult.testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Expected number of INSERT statements <5> but is <1>")
                .contains("insert")
                .contains("into")
                .contains("Book")
                .contains("isbn, title, id)");

    }

}
