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

package org.quickperf.perfrecording;

import java.util.*;

public class ExecutionOrderOfPerfRecorders {

    private final List<Class<? extends RecordablePerformance>> beforeTestMethod;

    private final List<Class<? extends RecordablePerformance>> afterTestMethod;

    public ExecutionOrderOfPerfRecorders(List<Class<? extends RecordablePerformance>> beforeTestMethod
                                       , List<Class<? extends RecordablePerformance>> afterTestMethod) {
        this.beforeTestMethod = beforeTestMethod;
        this.afterTestMethod = afterTestMethod;
    }

    public List<RecordablePerformance> sortPerfRecordersBeforeTestMethod(List<RecordablePerformance> unsortedPerfRecorder) {
        return sortPerfRecorders(unsortedPerfRecorder, beforeTestMethod);
    }

    private List<RecordablePerformance> sortPerfRecorders(List<RecordablePerformance> unsortedPerfRecorder
                                                       , List<Class<? extends RecordablePerformance>> orderedPerfRecorders) {
        Map<Class<? extends RecordablePerformance>, RecordablePerformance> perfRecorderInstanceByPerfRecorderClass
                = buildPerfRecorderInstanceByPerfRecorderClass(unsortedPerfRecorder);
        Set<Class<? extends RecordablePerformance>> perfRecorderClasses = perfRecorderInstanceByPerfRecorderClass.keySet();
        List<RecordablePerformance> sortedPerfRecorders = new ArrayList<>();
        for (Class<? extends RecordablePerformance> perfRecorderClass : orderedPerfRecorders) {
            if (perfRecorderClasses.contains(perfRecorderClass)) {
                sortedPerfRecorders.add(perfRecorderInstanceByPerfRecorderClass.get(perfRecorderClass));
            }
        }
        return sortedPerfRecorders;
    }

    private Map<Class<? extends RecordablePerformance>, RecordablePerformance> buildPerfRecorderInstanceByPerfRecorderClass(List<RecordablePerformance> perfRecorders) {
        Map<Class<? extends RecordablePerformance>, RecordablePerformance>
                perfRecorderInstanceByPerfRecorderClass = new HashMap<>();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            perfRecorderInstanceByPerfRecorderClass.put(perfRecorder.getClass(), perfRecorder);
        }
        return perfRecorderInstanceByPerfRecorderClass;
    }

    public List<RecordablePerformance> sortPerfRecordersAfterTestMethod(List<RecordablePerformance> unsortedPerfRecorder) {
        return sortPerfRecorders(unsortedPerfRecorder, afterTestMethod);
    }

}
