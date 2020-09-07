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

package org.quickperf.sql.framework.quickperf;

import org.quickperf.sql.framework.ClassPath;
import org.quickperf.sql.framework.QuickPerfSuggestion;

public class DataSourceConfig implements QuickPerfSuggestion {

    @Override
    public String getMessage() {

        String possibleConfigIssue =
                  "No SQL execution is detected."
                + System.lineSeparator()
                + "QuickPerf uses a proxy on the data source to intercept the SQL executions.";

        QuickPerfSuggestion frameworkConfig
                = DataSourceFrameworkConfigFactory.makeFrom(ClassPath.INSTANCE);

        return    possibleConfigIssue
                + System.lineSeparator()
                + frameworkConfig.getMessage();

    }

}