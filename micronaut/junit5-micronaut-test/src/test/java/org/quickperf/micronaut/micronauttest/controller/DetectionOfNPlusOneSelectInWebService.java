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

package org.quickperf.micronaut.micronauttest.controller;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.micronaut.micronauttest.dto.PlayerWithTeamName;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MicronautTest
@QuickPerfTest
public class DetectionOfNPlusOneSelectInWebService {

    @Inject
    @Client("/")
    RxHttpClient client;

    @ExpectSelect(1)
    @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
    @Test
    public void should_find_all_players() {

        // GIVEN

        // WHEN
        List<PlayerWithTeamName> players = client.toBlocking().retrieve(HttpRequest.GET("/players"), Argument.of(List.class, PlayerWithTeamName.class));

        // THEN
        assertThat(players).hasSize(2);

    }

}