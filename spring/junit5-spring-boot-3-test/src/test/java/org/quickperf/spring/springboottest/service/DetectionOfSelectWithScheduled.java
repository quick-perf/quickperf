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

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.assertj.core.api.Assertions.assertThat;

@QuickPerfTest
@SpringBootTest(classes = {FootballApplication.class, DetectionOfSelectWithScheduled.SchedulingConfig.class})
public class DetectionOfSelectWithScheduled {

    @TestConfiguration
    @EnableScheduling
    static class SchedulingConfig {
    }

    @Autowired
    private ScheduledCountService scheduledCountService;

    @ExpectSelect(0)
    @Test
    void should_detect_select_from_scheduled_task() throws InterruptedException {

        // GIVEN
        scheduledCountService.arm();

        // WHEN
        boolean executed = scheduledCountService.awaitExecution(5000);

        // THEN
        assertThat(executed).isTrue();

    }

}
