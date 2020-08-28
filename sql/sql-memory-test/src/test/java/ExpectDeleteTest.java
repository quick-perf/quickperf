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

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectDelete;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class ExpectDeleteTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SqlDelete extends SqlTestBase {

        @ExpectDelete(5)
        @Test
        public void execute_one_delete_but_five_deletes_expected() {

            EntityManager em = emf.createEntityManager();
            em.getTransaction().begin();

            Query query = em.createQuery("DELETE FROM " + Book.class.getCanonicalName());
            query.executeUpdate();

            em.getTransaction().commit();

        }

    }

    @Test public void
    should_fail_if_the_number_of_delete_statements_is_not_equal_to_the_number_expected() {

        // GIVEN
        Class<?> testClass = SqlDelete.class;

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                .contains("Expected number of DELETE statements <5> but is <1>")
                .contains("delete")
                .contains("from")
                .contains("Book");

    }

}
