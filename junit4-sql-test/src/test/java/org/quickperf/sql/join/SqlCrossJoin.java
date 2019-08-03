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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlTestBase;
import org.quickperf.sql.annotation.EnableCrossJoin;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(QuickPerfJUnitRunner.class)
public class SqlCrossJoin extends SqlTestBase {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodAnnotatedWithEnableCrossJoin extends SqlTestBase {

        @EnableCrossJoin
        @Test
        public void execute_one_cross_join() {
            EntityManager entityManager = emf.createEntityManager();
            String nativeQuery = "SELECT b1.* FROM Book b1 CROSS JOIN Book b2";
            Query query = entityManager.createNativeQuery(nativeQuery);
            query.getResultList();
        }

    }

    @Test
    public void execute_one_cross_join() {
        EntityManager entityManager = emf.createEntityManager();
        String nativeQuery = "SELECT b1.* FROM Book b1 CROSS JOIN Book b2";
        Query query = entityManager.createNativeQuery(nativeQuery);
        query.getResultList();
    }

}
