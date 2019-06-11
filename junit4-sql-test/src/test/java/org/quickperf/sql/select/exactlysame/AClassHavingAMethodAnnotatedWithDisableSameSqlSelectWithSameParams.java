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

package org.quickperf.sql.select.exactlysame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlTestBase;
import org.quickperf.sql.annotation.DisableExactlySameSelects;
import org.quickperf.sql.entities.Book;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(QuickPerfJUnitRunner.class)
public class AClassHavingAMethodAnnotatedWithDisableSameSqlSelectWithSameParams extends SqlTestBase {

    @Test
    @DisableExactlySameSelects
    public void execute_two_same_select_with_same_params() {

        EntityManager em = emf.createEntityManager();

        String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id = :idParam";

        Query query1 = em.createQuery(hqlQuery);
        query1.setParameter("idParam", 3L);
        query1.getResultList();

        Query query2 = em.createQuery(hqlQuery);
        query2.setParameter("idParam", 3L);
        query2.getResultList();

    }
}
