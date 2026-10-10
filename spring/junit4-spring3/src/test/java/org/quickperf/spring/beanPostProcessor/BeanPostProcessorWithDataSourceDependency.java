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
package org.quickperf.spring.beanPostProcessor;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Verifies that QuickPerf proxies the DataSource even when another
 * BeanPostProcessor directly depends on it.
 */
@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(classes = BeanPostProcessorWithDataSourceDependency.Config.class)
public class BeanPostProcessorWithDataSourceDependency {

    @Autowired
    private DataSource dataSource;

    @ExpectSelect(2)
    @Test
    public void should_proxy_datasource_even_when_other_bean_post_processor_depends_on_datasource() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1");
        }
    }

    static class OrderedBeanPostProcessorWithDataSourceDependency implements BeanPostProcessor, Ordered {

        // DataSource dependency forces early creation of the DataSource during BeanPostProcessor instantiation.
        OrderedBeanPostProcessorWithDataSourceDependency(DataSource dataSource) {
        }

        @Override
        public Object postProcessBeforeInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) {
            return bean;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }

    @Configuration
    static class Config {

        @Bean
        public DataSource dataSource() {
            BasicDataSource dataSource = new BasicDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl("jdbc:h2:mem:beanPostProcessorWithDataSourceDependency;DB_CLOSE_DELAY=-1");
            return dataSource;
        }

        @Bean
        public QuickPerfProxyBeanPostProcessor dataSourceBeanPostProcessor() {
            return new QuickPerfProxyBeanPostProcessor();
        }

        @Bean
        static OrderedBeanPostProcessorWithDataSourceDependency orderedBeanPostProcessorWithDataSourceDependency(DataSource dataSource) {
            return new OrderedBeanPostProcessorWithDataSourceDependency(dataSource);
        }
    }
}
