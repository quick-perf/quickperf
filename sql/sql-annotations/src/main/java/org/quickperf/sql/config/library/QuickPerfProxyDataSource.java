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

package org.quickperf.sql.config.library;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.quickperf.sql.connection.QuickPerfDatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class QuickPerfProxyDataSource extends ProxyDataSource {

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        QuickPerfDatabaseConnection connectionProxy = QuickPerfDatabaseConnection.buildFrom(connection);
        connectionProxy.theDatasourceGetsTheConnection();
        return connectionProxy;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        QuickPerfDatabaseConnection connectionProxy = QuickPerfDatabaseConnection.buildFrom(connection);
        connectionProxy.theDatasourceGetsTheConnection();
        return connectionProxy;
    }

}
