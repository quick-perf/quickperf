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
package org.quickperf.sql.framework.quickperf;

import org.quickperf.sql.framework.QuickPerfSuggestion;

class DefaultConfig implements QuickPerfSuggestion {

    @Override
    public String getMessage() {
        return    "Try to create the  proxy with QuickPerfSqlDataSourceBuilder and to use it "
                + System.lineSeparator() + "in the test, as shown in the following example:"
                + System.lineSeparator()
                + System.lineSeparator() + "\t\timport net.ttddyy.dsproxy.support.ProxyDataSource;"
                + System.lineSeparator() + "\t\timport org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;"
                + System.lineSeparator()
                + System.lineSeparator() + "\t\tDataSource dataSource = ...;"
                + System.lineSeparator()
                + System.lineSeparator() + "        ProxyDataSource proxyDataSource = QuickPerfSqlDataSourceBuilder.aDataSourceBuilder()"
                + System.lineSeparator() + "                                          .buildProxy(dataSource);"
                + System.lineSeparator() + "\t\t// Use ProxyDatasource in the test";
    }

}
