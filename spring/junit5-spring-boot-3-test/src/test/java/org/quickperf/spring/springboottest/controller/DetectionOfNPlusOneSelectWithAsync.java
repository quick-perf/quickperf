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

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.spring.springboottest.dto.PlayerWithTeamName;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuickPerfTest
@SpringBootTest(classes = {FootballApplication.class}
              , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class DetectionOfNPlusOneSelectWithAsync {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @ExpectSelect(1)
    @Test
    void should_detect_n_plus_one_select_via_async_endpoint() {

        // GIVEN
        String url = "http://localhost:" + port + "/async/players";

        // WHEN
        ParameterizedTypeReference<List<PlayerWithTeamName>> paramType
                = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<PlayerWithTeamName>> playersResponseEntity = restTemplate
                .exchange(url, HttpMethod.GET, null, paramType);

        // THEN
        assertThat(playersResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<PlayerWithTeamName> players = playersResponseEntity.getBody();
        assertThat(players).hasSize(2);

    }

}
