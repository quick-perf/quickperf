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

package org.quickperf.testng.sql;

import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.sql.annotation.ExpectSelect;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class TestNGClassWithAMethodAnnotatedWithDisableQuickPerf extends SqlTestBaseTestNG {

    @DisableQuickPerf
    @ExpectSelect(5)
    @Test
    public void execute_one_select_but_five_select_expected() {
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
        query.getResultList();
    }

}
