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

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;


/**
 * Verifies that each DataSource bean is still counted when the beans do not delegate to
 * each other.
 */
@QuickPerfTest
@SpringBootTest(classes = {FootballApplication.class})
@Import(QueryCountingWithIndependentDataSources.Config.class)
public class QueryCountingWithIndependentDataSources {

    @Autowired
    @Qualifier("firstDataSource")
    private DataSource firstDataSource;

    @Autowired
    @Qualifier("secondDataSource")
    private DataSource secondDataSource;

    @ExpectSelect(2)
    @Test
    void should_count_the_selects_of_each_datasource_bean_when_they_do_not_delegate() {
        executeOneSelectOnEachDataSource();
    }

    // Wrong on purpose. The failure message states how many selects were really recorded,
    // which pins the count instead of only checking that nothing failed.
    @ExpectSelect(3)
    @Test
    void should_report_the_number_of_recorded_selects() {
        executeOneSelectOnEachDataSource();
    }

    private void executeOneSelectOnEachDataSource() {
        String select = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES";

        new JdbcTemplate(firstDataSource).queryForObject(select, Integer.class);
        new JdbcTemplate(secondDataSource).queryForObject(select, Integer.class);
    }

    static class Config {

        @Bean
        @Primary
        DataSource firstDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .generateUniqueName(true)
                    .build();
        }

        @Bean
        DataSource secondDataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .generateUniqueName(true)
                    .build();
        }
    }
}
