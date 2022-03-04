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
package org.quickperf.sql.config;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.util.concurrent.ThreadLocalRandom;

public class MemoryDataSourceBuilder {

    private MemoryDataSourceBuilder() { }

    public static MemoryDataSourceBuilder aDataSource() {
        return new MemoryDataSourceBuilder();
    }

    public DataSource build() {
        int randomInt = Math.abs(ThreadLocalRandom.current().nextInt());
        String url = "jdbc:h2:mem:test" + randomInt;
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl(url);
        dataSource.setUsername("qp");
        dataSource.setPassword("");
        dataSource.setMaxActive(4);
        dataSource.setPoolPreparedStatements(true);
        return dataSource;
    }

}
