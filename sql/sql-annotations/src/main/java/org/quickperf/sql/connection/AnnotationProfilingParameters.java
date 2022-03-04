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
package org.quickperf.sql.connection;

import org.quickperf.perfrecording.PerfRecorderParameters;

public class AnnotationProfilingParameters implements PerfRecorderParameters {

    private final boolean beforeAndAfterTestMethodExecution;

    private final ProfilingParameters profilingParameters;

    public AnnotationProfilingParameters( ProfilingParameters profilingParameters
                                        , boolean profileBeforeAndTestMethodExecution) {
        this.profilingParameters = profilingParameters;
        this.beforeAndAfterTestMethodExecution = profileBeforeAndTestMethodExecution;

    }

    public boolean isBeforeAndAfterTestMethodExecution() {
        return beforeAndAfterTestMethodExecution;
    }

    public ProfilingParameters getProfilingParameters() {
        return profilingParameters;
    }

}
