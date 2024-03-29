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
package org.quickperf.config.library;

import org.quickperf.RecorderExecutionOrder;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.time.ExecutionTimeRecorder;
import org.quickperf.time.MaxExecutionTimeVerifier;
import org.quickperf.time.MeasureExecutionTimeReporter;

import java.util.Arrays;
import java.util.Collection;

public class CoreConfigLoader implements QuickPerfConfigLoader {

    public static final CoreConfigLoader INSTANCE = new CoreConfigLoader();

    private CoreConfigLoader() {}

    @Override
    public Collection<AnnotationConfig> loadAnnotationConfigs() {
        return Arrays.asList(
                  new AnnotationConfig.Builder()
                  .perfRecorderClass(ExecutionTimeRecorder.class)
                  .perfIssueVerifier(MeasureExecutionTimeReporter.INSTANCE)
                  .build(MeasureExecutionTime.class)
                ,
                new AnnotationConfig.Builder()
                  .perfRecorderClass(ExecutionTimeRecorder.class)
                  .perfIssueVerifier(MaxExecutionTimeVerifier.INSTANCE)
                  .build(ExpectMaxExecutionTime.class)
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersBeforeTestMethod() {
        return Arrays.asList(
                 new RecorderExecutionOrder(ExecutionTimeRecorder.class, 10_000)
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersAfterTestMethod() {
        return Arrays.asList(
                new RecorderExecutionOrder(ExecutionTimeRecorder.class, 1000)
        );
    }

}
