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
package org.quickperf.sql.connection.stack;

import org.quickperf.sql.config.library.QuickPerfProxyDataSource;
import org.quickperf.sql.connection.TestConnectionProfiler;

public class QuickPerfStackTraceTraceFilter implements StackTraceFilter {

    public static final QuickPerfStackTraceTraceFilter INSTANCE = new QuickPerfStackTraceTraceFilter();

    private QuickPerfStackTraceTraceFilter() { }

    @Override
    public PositionsFiltering filter(StackTraceElement[] stackElements) {
        short quickPerfFirstPos = extractQuickPerfFirstPos(stackElements);
        int lastStackElementPos = stackElements.length;
        return new PositionsFiltering(quickPerfFirstPos, lastStackElementPos);
    }

    private short extractQuickPerfFirstPos(StackTraceElement[] stackElements) {
        String quickPerfProxyDataSourcePackage = QuickPerfProxyDataSource.class.getPackage().getName();
        String connectionProfilerPackageName = TestConnectionProfiler.class.getPackage().getName();
        for (short pos = 0; pos < stackElements.length; pos++) {
            String stackTraceElementAsString = stackElements[pos].toString();
            if (  !stackTraceElementAsString.contains("java.lang.Thread")
               && !stackTraceElementAsString.contains(connectionProfilerPackageName)
               && !stackTraceElementAsString.contains(quickPerfProxyDataSourcePackage)) {
                return pos;
            }
        }
        return 0;
    }

}
