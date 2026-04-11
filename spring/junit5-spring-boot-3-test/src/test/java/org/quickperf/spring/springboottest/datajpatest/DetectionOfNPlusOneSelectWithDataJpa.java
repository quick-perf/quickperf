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
package org.quickperf.spring.springboottest.datajpatest;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.springboottest.jpa.entity.Player;
import org.quickperf.spring.springboottest.jpa.repository.PlayerRepository;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@QuickPerfTest
@DataJpaTest
public class DetectionOfNPlusOneSelectWithDataJpa {

    @Autowired
    private PlayerRepository playerRepository;

    @ExpectSelect(1)
    @Test
    public void should_find_all_players_with_team_name() {

        List<Player> players = playerRepository.findAll();

        List<String> teamNames = players.stream()
                .map(player -> player.getTeam().getName())
                .collect(Collectors.toList());

        assertThat(teamNames).hasSize(2);

    }

}
