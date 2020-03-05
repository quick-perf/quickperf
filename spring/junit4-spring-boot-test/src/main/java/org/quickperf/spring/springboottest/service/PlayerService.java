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

package org.quickperf.spring.springboottest.service;

import org.quickperf.spring.springboottest.dto.PlayerWithTeamName;
import org.quickperf.spring.springboottest.jpa.entity.Player;
import org.quickperf.spring.springboottest.jpa.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PlayerService {

    private PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerWithTeamName> findPlayersWithTeamName() {
        List<Player> players = playerRepository.findAll();
        return  players
               .stream()
               .map(player -> new PlayerWithTeamName(
                                    player.getFirstName()
                                  , player.getLastName()
                                  , player.getTeam().getName()
                              )
                    )
               .collect(Collectors.toList());
    }

}
