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
package org.quickperf.spring.springboottest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.jooq.test.autoconfigure.JooqTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

class SpringBoot4SqlTest {

    @ParameterizedTest
    @ValueSource(classes = {
            FullContext.class, JdbcSlice.class, DataJdbcSlice.class, DataJpaSlice.class, JooqSlice.class
    })
    void should_count_selects_with_spring_boot_4(Class<?> scenario) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(
                LauncherDiscoveryRequestBuilder.request().selectors(selectClass(scenario)).build(), listener);

        TestExecutionSummary result = listener.getSummary();
        assertThat(result.getFailures()).hasSize(1);
        assertThat(result.getFailures().get(0).getException())
                .hasMessageContaining("You may think that <1> select statement was sent to the database")
                .hasMessageContaining("there are in fact <2>");
        assertThat(result.getTestsSucceededCount()).isEqualTo(1);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }

    abstract static class SelectScenarios {

        @Autowired
        private DataSource dataSource;

        @Test
        @ExpectSelect(1)
        void should_pass_when_the_count_matches() {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
        }

        @Test
        @ExpectSelect(1)
        void should_fail_when_the_count_differs() {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            assertThat(jdbc.queryForObject("SELECT 1", Integer.class)).isEqualTo(1);
            assertThat(jdbc.queryForObject("SELECT 2", Integer.class)).isEqualTo(2);
        }
    }

    @QuickPerfTest
    @SpringBootTest
    static class FullContext extends SelectScenarios {
    }

    @QuickPerfTest
    @JdbcTest
    static class JdbcSlice extends SelectScenarios {
    }

    @QuickPerfTest
    @DataJdbcTest
    static class DataJdbcSlice extends SelectScenarios {
    }

    @QuickPerfTest
    @DataJpaTest
    static class DataJpaSlice extends SelectScenarios {
    }

    @QuickPerfTest
    @JooqTest
    static class JooqSlice extends SelectScenarios {
    }
}
