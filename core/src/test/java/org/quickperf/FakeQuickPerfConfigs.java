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
package org.quickperf;

import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.perfrecording.ExecutionOrderOfPerfRecorders;
import org.quickperf.perfrecording.PerfRecord;
import org.quickperf.perfrecording.RecordablePerformance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * QuickPerf configs made of test annotation configs. The core tests do not declare a class implementing
 * SpecifiableGlobalAnnotations: the class path scan of AnnotationsExtractor.INSTANCE would find it.
 */
class FakeQuickPerfConfigs {

    private FakeQuickPerfConfigs() { }

    /**
     * QuickPerf configs made of the given annotation configs. Their recorders execute in the order of the configs,
     * before and after the test method: ExecutionOrderOfPerfRecorders drops a recorder that it does not list.
     */
    static QuickPerfConfigs quickPerfConfigsOf(AnnotationConfig... annotationConfigs) {
        SetOfAnnotationConfigs testAnnotationConfigs = new SetOfAnnotationConfigs(Arrays.asList(annotationConfigs));
        List<Class<? extends RecordablePerformance>> recorderClasses = recorderClassesOf(annotationConfigs);
        ExecutionOrderOfPerfRecorders sameOrderBeforeAndAfterTheTestMethod =
                new ExecutionOrderOfPerfRecorders(recorderClasses, recorderClasses);
        return new QuickPerfConfigs(testAnnotationConfigs, sameOrderBeforeAndAfterTheTestMethod);
    }

    // Each recorder class once, in the order of the annotation configs. An annotation config can have no recorder.
    private static List<Class<? extends RecordablePerformance>> recorderClassesOf(AnnotationConfig[] configs) {
        Set<Class<? extends RecordablePerformance>> recorderClasses = new LinkedHashSet<>();
        for (AnnotationConfig config : configs) {
            if (config.getPerfRecorderClass() != null) {
                recorderClasses.add(config.getPerfRecorderClass());
            }
        }
        return new ArrayList<>(recorderClasses);
    }

    /**
     * A recorder recording nothing. TestExecutionContext instantiates the recorders without making their constructor
     * accessible: the recorders of this package, the package of TestExecutionContext, can be package-private, but a
     * recorder of another package must be public, with a public constructor.
     */
    static class FakeRecorder implements RecordablePerformance<PerfRecord> {

        @Override
        public void startRecording(TestExecutionContext testExecutionContext) { }

        @Override
        public void stopRecording(TestExecutionContext testExecutionContext) { }

        @Override
        public PerfRecord findRecord(TestExecutionContext testExecutionContext) {
            return PerfRecord.NONE;
        }

        @Override
        public void cleanResources() { }

    }

}
