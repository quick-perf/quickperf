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

package org.quickperf.jvm.config.library;

import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.jvm.allocation.MeasureHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.MaxHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.NoHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder;
import org.quickperf.jvm.annotations.*;
import org.quickperf.jvm.jfr.JfrEventsRecorder;
import org.quickperf.jvm.jmcrule.JmcRuleCountMeasureExtractor;
import org.quickperf.jvm.jmcrule.JmcRulesPerfVerifier;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.lang.annotation.Annotation;
import java.util.List;

class JvmAnnotationsConfigs {

    private JvmAnnotationsConfigs() { }

    private static class JfrAnnotationToJvmOptionConverter implements AnnotationToJvmOptionConverter {

        private static final JfrAnnotationToJvmOptionConverter INSTANCE = new JfrAnnotationToJvmOptionConverter();

        private JfrAnnotationToJvmOptionConverter() { }

        @Override
        public List<JvmOption> convertToJvmOptions(Annotation annotation) {
            return JfrJvmOptions.INSTANCE.getValues();
        }

    }

    static final AnnotationConfig XMS = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(XmsAnnotToJvmOptionConverter.INSTANCE)
            .build(Xms.class);

    static final AnnotationConfig XMX = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(XmxAnnotToJvmOptionConverter.INSTANCE)
            .build(Xmx.class);

    static final AnnotationConfig HEAP_SIZE = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(HeapSizeAnnotToJvmOptionConverter.INSTANCE)
            .build(HeapSize.class);

    static final AnnotationConfig JVM_OPTIONS = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(JvmOptionsAnnotToJvmOptionConverter.INSTANCE)
            .build(JvmOptions.class);

    static final AnnotationConfig DISPLAY_ALLOCATION_BY_BYTE_WATCHER = new AnnotationConfig.Builder()
            .perfRecorderClass(ByteWatcherRecorder.class)
            .perfIssueVerifier(MeasureHeapAllocationPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm()
            .build(MeasureHeapAllocation.class);

    static final AnnotationConfig MAX_ALLOCATION_BY_BYTE_WATCHER = new AnnotationConfig.Builder()
            .perfRecorderClass(ByteWatcherRecorder.class)
            .perfIssueVerifier(MaxHeapAllocationPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm()
            .build(ExpectMaxHeapAllocation.class);

    static final AnnotationConfig NO_ALLOCATION_BY_BYTE_WATCHER = new AnnotationConfig.Builder()
            .perfRecorderClass(ByteWatcherRecorder.class)
            .perfIssueVerifier(NoHeapAllocationPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm()
            .build(ExpectNoHeapAllocation.class);

    static final AnnotationConfig CHECK_JVM = new AnnotationConfig.Builder()
            .perfRecorderClass(JfrEventsRecorder.class)
            .perfMeasureExtractor(JmcRuleCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(JmcRulesPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm(JfrAnnotationToJvmOptionConverter.INSTANCE)
            .build(ExpectNoJvmIssue.class);

    static final AnnotationConfig PROFILE_JVM_WITH_JFR = new AnnotationConfig.Builder()
            .perfRecorderClass(JfrEventsRecorder.class)
            .testHasToBeLaunchedInASpecificJvm(JfrAnnotationToJvmOptionConverter.INSTANCE)
            .build(ProfileJvm.class);

    static final AnnotationConfig PROFILE_QUICK_PERF_WITH_JMC = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(QuickPerfProfilingAnnotToJvmOptionConverter.INSTANCE)
            .build(ProfileQuickPerfInTestJvm.class);

}
