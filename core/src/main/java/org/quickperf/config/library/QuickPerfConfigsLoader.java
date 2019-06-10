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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.config.library;
import org.quickperf.RecorderExecutionOrder;
import org.quickperf.perfrecording.ExecutionOrderOfPerfRecorders;
import org.quickperf.perfrecording.RecordablePerformance;

import java.util.*;

public class QuickPerfConfigsLoader {

    public static QuickPerfConfigsLoader INSTANCE = new QuickPerfConfigsLoader();

    private QuickPerfConfigsLoader() { }

    public QuickPerfConfigs loadQuickPerfConfigs() {

        List<AnnotationConfig> loadedAnnotationConfigs = new ArrayList<>();

        ServiceLoader<QuickPerfConfigLoader> loadedServices = ServiceLoader.load(QuickPerfConfigLoader.class);

        Iterator<QuickPerfConfigLoader> configLoaderIterator = loadedServices.iterator();

        List<RecorderExecutionOrder> executionOrderOfPerfRecordersBeforeTestMethod
                = new ArrayList<>();
        List<RecorderExecutionOrder> executionOrderOfPerfRecordersAfterTestMethod
                = new ArrayList<>();
        while(configLoaderIterator.hasNext()) {
            QuickPerfConfigLoader quickPerfConfigLoader = configLoaderIterator.next();
            Collection<AnnotationConfig> annotationConfigs = quickPerfConfigLoader.loadAnnotationConfigs();
            loadedAnnotationConfigs.addAll(annotationConfigs);
            executionOrderOfPerfRecordersBeforeTestMethod.addAll(quickPerfConfigLoader.loadRecorderExecutionOrdersBeforeTestMethod());
            executionOrderOfPerfRecordersAfterTestMethod.addAll(quickPerfConfigLoader.loadRecorderExecutionOrdersAfterTestMethod());
        }

        Collections.sort(executionOrderOfPerfRecordersBeforeTestMethod);
        Collections.sort(executionOrderOfPerfRecordersAfterTestMethod);

        SetOfAnnotationConfigs testAnnotationConfigs = new SetOfAnnotationConfigs(loadedAnnotationConfigs);

        List<Class<? extends RecordablePerformance>> perfRecordersOrderBeforeTestMethod = new ArrayList<>();
        for (RecorderExecutionOrder recorderExecutionOrder : executionOrderOfPerfRecordersBeforeTestMethod) {
            Class<? extends RecordablePerformance> perfRecorderClass = recorderExecutionOrder.getPerfRecorderClass();
            perfRecordersOrderBeforeTestMethod.add(perfRecorderClass);
        }


        List<Class<? extends RecordablePerformance>> perfRecordersOrderAfterTestMethod = new ArrayList<>();
        for (RecorderExecutionOrder recorderExecutionOrder : executionOrderOfPerfRecordersAfterTestMethod) {
            Class<? extends RecordablePerformance> perfRecorderClass = recorderExecutionOrder.getPerfRecorderClass();
            perfRecordersOrderAfterTestMethod.add(perfRecorderClass);
        }

        ExecutionOrderOfPerfRecorders execOrderForPerfRecorders
                = new ExecutionOrderOfPerfRecorders(perfRecordersOrderBeforeTestMethod,
                                                    perfRecordersOrderAfterTestMethod);

        return new QuickPerfConfigs(testAnnotationConfigs, execOrderForPerfRecorders);
    }

}
