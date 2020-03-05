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

package org.quickperf.config.library;

import org.quickperf.perfrecording.ExecutionOrderOfPerfRecorders;

public class QuickPerfConfigs {

    private final SetOfAnnotationConfigs testAnnotationConfigs;

    private final ExecutionOrderOfPerfRecorders executionOrderOfPerfRecorders;

    public QuickPerfConfigs( SetOfAnnotationConfigs testAnnotationConfigs
                           , ExecutionOrderOfPerfRecorders executionOrderOfPerfRecorders
    ) {
        this.executionOrderOfPerfRecorders = executionOrderOfPerfRecorders;
        this.testAnnotationConfigs = testAnnotationConfigs;
    }

    public SetOfAnnotationConfigs getTestAnnotationConfigs() {
        return testAnnotationConfigs;
    }

    public ExecutionOrderOfPerfRecorders getExecutionOrderOfPerfRecorders() {
        return executionOrderOfPerfRecorders;
    }

}
