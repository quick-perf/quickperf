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

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.ChainListener;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.proxy.ProxyConfig;
import net.ttddyy.dsproxy.proxy.ProxyJdbcObject;
import org.quickperf.sql.SqlRecorder;
import org.quickperf.sql.SqlRecorderRegistry;

import java.sql.Statement;
import java.util.Collection;
import java.util.List;

class DataSourceQuickPerfListener implements QueryExecutionListener {

    // ProxyJdbcObject.getProxyConfig() exists since datasource-proxy 1.10, but an application
    // can resolve an older datasource-proxy version than the one QuickPerf declares, for example
    // by declaring it itself. Calling the method would then throw a NoSuchMethodError from
    // afterQuery, which datasource-proxy calls in a finally block, so every query executed
    // through another datasource-proxy proxy would fail. With an older version, the check is
    // skipped and nested QuickPerf proxies record the queries twice, as before.
    private static final boolean PROXY_CONFIG_AVAILABLE = isProxyConfigAvailable();

    @Override
    public void beforeQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) {}


    @Override
    @SuppressWarnings("unchecked")
    public void afterQuery(ExecutionInfo executionInfo, List<QueryInfo> queries) {

        if (PROXY_CONFIG_AVAILABLE && isRecordedByAnInnerQuickPerfProxy(executionInfo.getStatement())) {
            return;
        }

        Collection<SqlRecorder> sqlRecorders = SqlRecorderRegistry.INSTANCE.getSqlRecorders();

        for (SqlRecorder sqlRecorder : sqlRecorders) {
            sqlRecorder.addQueryExecution(executionInfo, queries);
        }

    }

    // When this proxy wraps another QuickPerf proxy, directly or through
    // wrappers handing over its statements, the statement executed here is a
    // statement of the inner proxy, which records the same execution.
    private static boolean isRecordedByAnInnerQuickPerfProxy(Statement statement) {
        Object current = statement;
        while (current instanceof ProxyJdbcObject) {
            ProxyJdbcObject proxy = (ProxyJdbcObject) current;
            ProxyConfig proxyConfig = proxy.getProxyConfig();
            ChainListener queryListener = proxyConfig.getQueryListener();
            if (isOrChainsAQuickPerfListener(queryListener)) {
                return true;
            }
            current = proxy.getTarget();
        }
        return false;
    }

    private static boolean isOrChainsAQuickPerfListener(QueryExecutionListener listener) {
        if (listener instanceof ChainListener) {
            for (QueryExecutionListener chainedListener : ((ChainListener) listener).getListeners()) {
                if (isOrChainsAQuickPerfListener(chainedListener)) {
                    return true;
                }
            }
        }
        return listener instanceof DataSourceQuickPerfListener;
    }

    private static boolean isProxyConfigAvailable() {
        try {
            ProxyJdbcObject.class.getMethod("getProxyConfig");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}