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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@QuickPerfTest
@SpringBootTest(classes = {FootballApplication.class}
              , webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class CrossTestContaminationWithConcurrentRandomPort {

    // Counted down by the no-SQL test from its test method body, where its
    // recorder is registered, so that the SQL-firing test does not run its
    // work until both recorders are simultaneously registered (the
    // contamination trigger condition). Counting down in @BeforeEach would
    // be too early: QuickPerf registers the recorders after @BeforeEach.
    private static volatile CountDownLatch NO_SQL_TEST_RECORDING;

    // Released by the SQL-firing test's @AfterEach AFTER its Tomcat-worker
    // SQL has flushed and its recorder has been unregistered. The no-SQL
    // test awaits this so its assertion observes the post-flush state.
    private static volatile CountDownLatch WORKER_DONE;

    // Per run, so that the tests wait for each other again when they are
    // executed again in the same JVM (IDE repeat, Surefire
    // rerunFailingTestsCount)
    @BeforeAll
    static void createLatches() {
        NO_SQL_TEST_RECORDING = new CountDownLatch(1);
        WORKER_DONE = new CountDownLatch(1);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @ExpectSelect(3)
    @Test
    void test_with_select() throws InterruptedException {
        // Wait until both tests are registered, so the Tomcat worker
        // takes the active-set fallback with two distinct owners live.
        assertThat(NO_SQL_TEST_RECORDING.await(30, TimeUnit.SECONDS))
                .as("both tests registered")
                .isTrue();
        // 1 SELECT for players + 2 SELECTs for lazy-loaded teams (N+1)
        String url = "http://localhost:" + port + "/players";
        ParameterizedTypeReference<List<PlayerWithTeamName>> paramType
                = new ParameterizedTypeReference<>() {};
        ResponseEntity<List<PlayerWithTeamName>> response = restTemplate
                .exchange(url, HttpMethod.GET, null, paramType);
    }

    @AfterEach
    void releaseWorkerDone() {
        WORKER_DONE.countDown();
    }

    @ExpectSelect(0)
    @Test
    void test_with_no_sql() throws InterruptedException {
        NO_SQL_TEST_RECORDING.countDown();
        // Keep this test's recorder active until the Tomcat worker of the
        // sibling test has flushed and unregistered, then run the
        // @ExpectSelect(0) assertion. The latch replaces a brittle
        // wall-clock Thread.sleep, removing slow-CI / debug-attach flakes.
        WORKER_DONE.await(30, TimeUnit.SECONDS);
    }

}
