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
public class AClassHavingAMethodAnnotatedWithDisableSameSqlForDifferentParamValues extends SqlTestBase {

    @Test
    @DisableExactlySameSelects
    public void execute_two_select_with_different_param_values() {
        EntityManager em = emf.createEntityManager();

        String hqlQuery = "FROM " + Book.class.getCanonicalName() + " b WHERE b.id=:idParam AND b.title=:titleParam";

        Query query1 = em.createQuery(hqlQuery);
        query1.setParameter("idParam", 3L);
        query1.setParameter("titleParam", "titleParam1");
        query1.getResultList();

        Query query2 = em.createQuery(hqlQuery);
        query2.setParameter("idParam", 1L);
        query2.setParameter("titleParam", "titleParam1");
        query2.getResultList();

    }

    @Test
    @DisableExactlySameSelects
    public void execute_two_selects_with_different_params() {

        EntityManager em = emf.createEntityManager();
        String select = "FROM " + Book.class.getCanonicalName() + " b ";

        Query query1 = em.createQuery(select + "WHERE b.title=:title");
        query1.setParameter("title", "aTitle");
        query1.getResultList();

        Query query2 = em.createQuery(select + "WHERE b.isbn=:isbn");
        query2.setParameter("isbn", "anIsbn");
        query2.getResultList();

    }

}
