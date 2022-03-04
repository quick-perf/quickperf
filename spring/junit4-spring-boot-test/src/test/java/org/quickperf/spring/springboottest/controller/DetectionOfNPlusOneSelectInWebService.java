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
package org.quickperf.spring.springboottest.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.spring.springboottest.dto.PlayerWithTeamName;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(QuickPerfSpringRunner.class)
@SpringBootTest(classes = {FootballApplication.class}
              , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class DetectionOfNPlusOneSelectInWebService {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @ExpectSelect(1)
    @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
    @Test
    public void should_find_all_players() {

        // GIVEN
        String getUrl = "http://localhost:" + port + "/players";

        // WHEN
        ResponseEntity<List> playersResponseEntity = restTemplate.getForEntity(getUrl, List.class);
        List<PlayerWithTeamName> players = playersResponseEntity.getBody();

        // THEN
        assertThat(playersResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(players).hasSize(2);

    }

}