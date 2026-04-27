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
package org.quickperf.spring.springboottest.beanPostProcessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.springboottest.FootballApplication;
import org.quickperf.spring.springboottest.jpa.entity.Player;
import org.quickperf.spring.springboottest.jpa.repository.PlayerRepository;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that QuickPerf proxies the DataSource even when another
 * BeanPostProcessor directly depends on it.
 */
@RunWith(QuickPerfSpringRunner.class)
@SpringBootTest(classes = {FootballApplication.class})
@Import(BeanPostProcessorWithDataSourceDependency.Config.class)
public class BeanPostProcessorWithDataSourceDependency {

    @Autowired
    private PlayerRepository playerRepository;

    @ExpectSelect(2)
    @Test
    public void should_proxy_datasource_even_when_other_bean_post_processor_depends_on_datasource() {
        List<Player> players = playerRepository.findAll();
        assertThat(players).hasSize(2);
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

    static class Config {

        // Must be static so Spring detects this BeanPostProcessor during registerBeanPostProcessors()
        @Bean
        static OrderedBeanPostProcessorWithDataSourceDependency orderedBeanPostProcessorWithDataSourceDependency(DataSource dataSource) {
            return new OrderedBeanPostProcessorWithDataSourceDependency(dataSource);
        }
    }
}
