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
package org.quickperf.spring.springboottest.service;

import org.quickperf.spring.springboottest.dto.PlayerWithTeamName;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AsyncPlayerService {

    private final PlayerService playerService;

    public AsyncPlayerService(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Async
    public CompletableFuture<List<PlayerWithTeamName>> findPlayersWithTeamNameAsync() {
        List<PlayerWithTeamName> result = playerService.findPlayersWithTeamName();
        return CompletableFuture.completedFuture(result);
    }

}
