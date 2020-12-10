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

package org.quickperf.spring.springboottest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.annotation.DisableGlobalAnnotations;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.springboottest.jpa.entity.Player;
import org.quickperf.spring.springboottest.jpa.repository.PlayerRepository;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(QuickPerfSpringRunner.class)
@Import(QuickPerfSqlConfig.class)
@DataJpaTest(showSql=false)
@DisableGlobalAnnotations
public class QuickPerfSpringRunnerBeforeAfterTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private PlayerRepository playerRepository;

    @Before
    public void before() {
        Player player = new Player();
        testEntityManager.persist(player);
    }

    @Test
    public void should_find_all_players() {
        List<Player> players = playerRepository.findAll();
        assertThat(players).hasSize(3);
    }

    @After
    public void after() {
        Player player = new Player();
        testEntityManager.persist(player);
    }

}
