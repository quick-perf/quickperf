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

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quickperf.spring.junit4.QuickPerfSpringRunner;
import org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.anH2DataSource;
import static org.quickperf.spring.nestedproxies.NestedProxiesTestSupport.executeOneSelect;

/**
 * Verifies that the select of a datasource-proxy ProxyDataSource bean of the application is recorded.
 */
@RunWith(QuickPerfSpringRunner.class)
@ContextConfiguration(classes = ApplicationProxyDataSourceBean.Config.class)
public class ApplicationProxyDataSourceBean {

    @Autowired
    private DataSource dataSource;

    @ExpectSelect(1)
    @Test
    public void execute_one_select() throws SQLException {
        executeOneSelect(dataSource);
    }

    @Configuration
    static class Config {

        @Bean
        public DataSource dataSource() {
            return new ProxyDataSource(anH2DataSource("application-proxy-datasource-bean"));
        }

        @Bean
        public static QuickPerfProxyBeanPostProcessor dataSourceBeanPostProcessor() {
            return new QuickPerfProxyBeanPostProcessor();
        }
    }
}
