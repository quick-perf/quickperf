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
package org.quickperf.spring.springboottest.datasourcedelegation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.spring.springboottest.jpa.entity.Player;
import org.quickperf.spring.springboottest.jpa.repository.PlayerRepository;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that a select is counted once when a DataSource bean delegates to another
 * DataSource bean.
 */
@RunWith(QuickPerfSpringRunner.class)
// DataSourceAutoConfiguration is excluded because it asks for the primary DataSource while the
// delegate is being initialized, which the two beans below turn into a circular reference.
@SpringBootTest(classes = {FootballApplication.class},
                properties = "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@Import(QueryCountingWithDelegatingDataSource.Config.class)
public class QueryCountingWithDelegatingDataSource {

    @Autowired
    private PlayerRepository playerRepository;

    @ExpectSelect(1)
    @Test
    public void should_count_a_select_once_when_a_datasource_bean_delegates_to_another_one() {
        List<Player> players = playerRepository.findAll();
        assertThat(players).hasSize(2);
    }

    // Wrong on purpose. The failure message states how many selects were really recorded,
    // which pins the count instead of only checking that nothing failed.
    @ExpectSelect(2)
    @Test
    public void should_report_the_number_of_recorded_selects() {
        playerRepository.findAll();
    }

    static class Config {

        @Bean
        DataSource targetDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.HSQL)
                    .generateUniqueName(true)
                    .build();
        }

        // LazyConnectionDataSourceProxy is a common way to end up with this setup.
        @Bean
        @Primary
        DataSource delegatingDataSource(@Qualifier("targetDataSource") DataSource targetDataSource) {
            return new LazyConnectionDataSourceProxy(targetDataSource);
        }
    }
}
