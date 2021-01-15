/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.sql.config;

import java.util.Properties;

public class HibernateConfigBuilder {

    private boolean showSql;

    private boolean formatSql;

    private boolean isBatched;

    private int batchSize;

    private HibernateConfigBuilder() { }

    public static HibernateConfigBuilder anHibernateConfig() {
        return new HibernateConfigBuilder();
    }

    public HibernateConfigBuilder withShowSql() {
        HibernateConfigBuilder newConfigBuilder = new HibernateConfigBuilder();
        newConfigBuilder.showSql = true;
        newConfigBuilder.formatSql = this.formatSql;
        newConfigBuilder.isBatched = this.isBatched;
        newConfigBuilder.batchSize = this.batchSize;
        return newConfigBuilder;
    }

    public HibernateConfigBuilder withFormatSql() {
        HibernateConfigBuilder newConfigBuilder = new HibernateConfigBuilder();
        newConfigBuilder.showSql = this.showSql;
        newConfigBuilder.formatSql = true;
        newConfigBuilder.isBatched = this.isBatched;
        newConfigBuilder.batchSize = this.batchSize;
        return newConfigBuilder;
    }

    public HibernateConfigBuilder withBatchSize(int batchSize) {
        HibernateConfigBuilder newConfigBuilder = new HibernateConfigBuilder();
        newConfigBuilder.showSql = this.showSql;
        newConfigBuilder.formatSql = this.formatSql;
        newConfigBuilder.isBatched = true;
        newConfigBuilder.batchSize = batchSize;
        return newConfigBuilder;
    }

    public Properties build(String dialect) {
        Properties config = new Properties();
        config.setProperty("hibernate.hbm2ddl.auto", "create");
        config.setProperty("hibernate.dialect", dialect);
        config.put("hibernate.show_sql", showSql);
        config.put("hibernate.format_sql", formatSql);
        if(isBatched) {
            config.put("hibernate.jdbc.batch_size", batchSize);
        }
        return config;
    }

}
