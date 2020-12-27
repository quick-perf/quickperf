/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.sql.config;

import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.quickperf.sql.config.library.QuickPerfProxyDataSource;

import javax.sql.DataSource;

public class QuickPerfSqlDataSourceBuilder {

    public static QuickPerfSqlDataSourceBuilder aDataSourceBuilder() {
        return new QuickPerfSqlDataSourceBuilder();
    }

    private QuickPerfSqlDataSourceBuilder() {}

    public ProxyDataSource buildProxy(DataSource dataSource) {

        QuickPerfProxyDataSource proxyDataSource = new QuickPerfProxyDataSource();

        ChainListener chainListener = new ChainListener();

        DataSourceQuickPerfListener dataSourceQuickPerfListener = new DataSourceQuickPerfListener();
        chainListener.addListener(dataSourceQuickPerfListener);

        proxyDataSource.addListener(chainListener);

        proxyDataSource.setDataSource(dataSource);

        return proxyDataSource;

    }

}
