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

package org.quickperf.sql.update;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.SqlTestBase;
import org.quickperf.sql.annotation.ExpectUpdate;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@RunWith(QuickPerfJUnitRunner.class)
public class SqlUpdate extends SqlTestBase {

    @ExpectUpdate(5)
    @Test
    public void execute_one_update_but_five_expected() {

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        String sql =   " UPDATE book"
                     + " SET isbn ='978-0134685991'"
                     + " WHERE id = 1";
        Query query = em.createNativeQuery(sql);

        query.executeUpdate();

        em.getTransaction().commit();

    }

}
