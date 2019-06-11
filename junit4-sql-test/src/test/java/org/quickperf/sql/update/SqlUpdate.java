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
import org.quickperf.sql.entities.Book;

import javax.persistence.EntityManager;

@RunWith(QuickPerfJUnitRunner.class)
public class SqlUpdate extends SqlTestBase {

    @ExpectUpdate(5)
    @Test
    public void execute_one_update_but_five_expected() {

        EntityManager em = emf.createEntityManager();

        Book effectiveJava = new Book();
        effectiveJava.setIsbn("effectiveJavaIsbn"); //TODO: TO MODIFY
        effectiveJava.setTitle("Effective Java");

        persist(em, effectiveJava);

        effectiveJava.setTitle("New Title");

        update(em, effectiveJava);

    }

    private void update(EntityManager em, Book effectiveJava) {
        em.merge(effectiveJava);
        em.getTransaction().begin();
        em.persist(effectiveJava);
        em.getTransaction().commit();
    }

    private void persist(EntityManager em, Book effectiveJava) {
        em.getTransaction().begin();
        em.persist(effectiveJava);
        em.getTransaction().commit();
    }

}
