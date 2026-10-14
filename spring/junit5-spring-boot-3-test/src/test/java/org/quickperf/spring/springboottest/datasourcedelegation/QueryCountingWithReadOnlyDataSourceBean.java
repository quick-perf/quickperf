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
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.quickperf.spring.springboottest.datasourcedelegation.ReadOnlyRoute.anH2DataSource;
import static org.quickperf.spring.springboottest.datasourcedelegation.ReadOnlyRoute.selectThrough;
import static org.quickperf.spring.springboottest.datasourcedelegation.ReadOnlyRoute.withReadOnlyTable;

/**
 * Verifies that a select is counted once on both routes when both DataSources are beans.
 */
@QuickPerfTest
@SpringBootTest(classes = {FootballApplication.class})
@Import(QueryCountingWithReadOnlyDataSourceBean.Config.class)
public class QueryCountingWithReadOnlyDataSourceBean {

    @Autowired
    private DataSource dataSource;

    @ExpectSelect(1)
    @Test
    void should_count_a_select_once_through_the_target_datasource() throws SQLException {
        selectThrough(dataSource, false);
    }

    @ExpectSelect(1)
    @Test
    void should_count_a_select_once_through_the_read_only_datasource() throws SQLException {
        selectThrough(dataSource, true);
    }

    // Wrong on purpose. The failure message states how many selects were really recorded,
    // which pins the count instead of only checking that nothing failed.
    @ExpectSelect(2)
    @Test
    void should_report_the_number_of_recorded_selects_through_the_read_only_datasource() throws SQLException {
        selectThrough(dataSource, true);
    }

    static class Config {

        @Bean
        DataSource targetDataSource() {
            return anH2DataSource();
        }

        @Bean
        DataSource readOnlyDataSource() {
            return withReadOnlyTable(anH2DataSource());
        }

        @Bean
        @Primary
        DataSource delegatingDataSource(@Qualifier("targetDataSource") DataSource targetDataSource,
                                        @Qualifier("readOnlyDataSource") DataSource readOnlyDataSource) {
            LazyConnectionDataSourceProxy delegatingDataSource = new LazyConnectionDataSourceProxy(targetDataSource);
            delegatingDataSource.setReadOnlyDataSource(readOnlyDataSource);
            return delegatingDataSource;
        }
    }
}
