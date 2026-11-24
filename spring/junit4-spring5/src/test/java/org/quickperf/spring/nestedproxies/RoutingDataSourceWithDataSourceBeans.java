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
package org.quickperf.spring.nestedproxies;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.connection.Level;
import org.quickperf.writer.WriterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.anH2DataSource;
import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.executeOneSelect;
import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.profilingFilePath;

/**
 * Verifies that the select and connection events of a routing DataSource bean are recorded once
 * when its target DataSources are beans. The post-processor cannot find the targets from the
 * routing DataSource, so the routing DataSource and its targets are all proxied.
 */
@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(classes = RoutingDataSourceWithDataSourceBeans.Config.class)
public class RoutingDataSourceWithDataSourceBeans {

    public static final String PROFILING_FILE_PATH = profilingFilePath("routing-datasource-with-datasource-beans-profiling.txt");

    @Autowired
    private DataSource dataSource;

    @ExpectSelect(1)
    @ProfileConnection(level = Level.TRACE, writerFactory = ProfilingFileWriter.class)
    @Test
    public void execute_one_select() throws SQLException {
        executeOneSelect(dataSource);
    }

    public static class ProfilingFileWriter implements WriterFactory {
        @Override
        public Writer buildWriter() throws IOException {
            return new FileWriter(PROFILING_FILE_PATH);
        }
    }

    public static class RoutingToTheFirstDataSource extends AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            return "first";
        }
    }

    @Configuration
    static class Config {

        @Bean
        public DataSource firstDataSource() {
            return anH2DataSource("routing-datasource-first");
        }

        @Bean
        public DataSource secondDataSource() {
            return anH2DataSource("routing-datasource-second");
        }

        @Bean
        @Primary
        public DataSource routingDataSource() {
            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put("first", firstDataSource());
            targetDataSources.put("second", secondDataSource());
            RoutingToTheFirstDataSource routingDataSource = new RoutingToTheFirstDataSource();
            routingDataSource.setTargetDataSources(targetDataSources);
            return routingDataSource;
        }

        @Bean
        public static QuickPerfProxyBeanPostProcessor dataSourceBeanPostProcessor() {
            return new QuickPerfProxyBeanPostProcessor();
        }
    }
}
