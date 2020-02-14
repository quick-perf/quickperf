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
 * Copyright 2020-2020 the original author or authors.
 */

package org.quickperf.sql.config;

import net.ttddyy.dsproxy.ConnectionInfo;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.proxy.JdbcProxyFactory;
import net.ttddyy.dsproxy.support.ProxyDataSource;

import java.sql.Connection;

public class QuickPerfSqlConnectionBuilder {
    public static QuickPerfSqlConnectionBuilder connectionBuilder() {
        return new QuickPerfSqlConnectionBuilder();
    }

    private QuickPerfSqlConnectionBuilder() {}

    public Connection buildProxy(Connection connection) {

        final ConnectionInfo connectionInfo = new ConnectionInfo();

        ProxyDataSource proxyDataSource = new ProxyDataSource();
        ChainListener chainListener = new ChainListener();
        DataSourceQuickPerfListener dataSourceQuickPerfListener = new DataSourceQuickPerfListener();
        chainListener.addListener(dataSourceQuickPerfListener);
        proxyDataSource.addListener(chainListener);

        return JdbcProxyFactory.DEFAULT.createConnection(connection, connectionInfo, proxyDataSource.getProxyConfig());
    }
}
