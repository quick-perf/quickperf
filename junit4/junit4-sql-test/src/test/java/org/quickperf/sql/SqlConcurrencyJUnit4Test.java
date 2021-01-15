/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ConcurrentTestRunner.class)
public class SqlConcurrencyJUnit4Test {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AClassHavingAMethodWithoutSqlPerformanceIssue extends SqlTestBaseJUnit4 {

        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            EntityManager em = emf.createEntityManager();
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }

    }
    
    @ThreadCount(100) @Test public void
    sql_performance_property_is_ok() {

        Class<?> testClass = AClassHavingAMethodWithoutSqlPerformanceIssue.class;

        PrintableResult printableResult = PrintableResult.testResult(testClass);

        assertThat(printableResult.failureCount()).isZero();

    }

}
