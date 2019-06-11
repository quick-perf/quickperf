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

package org.quickperf.sql.sametype;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlTestBase;
import org.quickperf.sql.annotation.DisableSameSelectTypesWithDifferentParams;
import org.quickperf.sql.entities.Book;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(QuickPerfJUnitRunner.class)
public class AClassHavingASameParamsMethodAnnotatedWithDisableSameSelectTypeWithDifferentParams extends SqlTestBase {

    @Test
    @DisableSameSelectTypesWithDifferentParams
    public void execute_two_same_select_with_two_same_params() {
        EntityManager em = emf.createEntityManager();

        String hqlQuery =   " FROM " + Book.class.getCanonicalName() + " b"
                          + " WHERE b.id=:idParam";

        Query query = em.createQuery(hqlQuery);
        query.setParameter("idParam", 2L);
        query.getResultList();

        Query query2 = em.createQuery(hqlQuery);
        query2.setParameter("idParam", 2L);
        query2.getResultList();

    }

}
