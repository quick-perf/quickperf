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
 * Copyright 2020-2020 the original author or authors.
 */

package org.quickperf.micronaut.micronauttest.service;

import io.micronaut.spring.tx.annotation.Transactional;
import org.quickperf.micronaut.micronauttest.dto.PlayerWithTeamName;
import org.quickperf.micronaut.micronauttest.jpa.entity.Player;
import org.quickperf.micronaut.micronauttest.jpa.repository.PlayerRepository;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
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
