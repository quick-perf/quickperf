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
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.spring.database;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.sql.annotation.ExpectInsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(classes = SpringConfig.class)
@TestExecutionListeners({TransactionalTestExecutionListener.class})
@Transactional
public class ClassWithAFailingTestAndTransactionalTestExecutionListener extends AbstractJUnit4SpringContextTests {

    @Autowired
    private UserDao userDao;

    @ExpectInsert(0)
    @Test
    public void user_is_persisted_and_retrieved_by_email() {

        // GIVEN
        String name = "Paul";
        String email = "paul@domain.com";

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        // WHEN
        userDao.save(user);
        User foundUser = userDao.findByEmail(email);

        // THEN
        assertThat(foundUser.getName()).isEqualTo(name);

    }

}
