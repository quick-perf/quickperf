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
package org.quickperf.jvm.config.library;

import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.jvm.allocation.MaxHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.MeasureHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.NoHeapAllocationPerfVerifier;
import org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder;
import org.quickperf.jvm.annotations.*;
import org.quickperf.jvm.gc.EnableGcLoggingAnnotToJvmOptionConverter;
import org.quickperf.jvm.gc.UseGcAnnotToJvmOptionConverter;
import org.quickperf.jvm.rss.ExpectMaxRssPerfVerifier;
import org.quickperf.jvm.rss.MeasureRssPerfVerifier;
import org.quickperf.jvm.rss.ProcessStatusRecorder;

public class JvmAnnotationsConfigs {

    private JvmAnnotationsConfigs() { }

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

    static final AnnotationConfig USE_GC = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(UseGcAnnotToJvmOptionConverter.INSTANCE)
            .build(UseGC.class);

    static final AnnotationConfig ENABLE_GC_LOGGING = new AnnotationConfig.Builder()
            .testHasToBeLaunchedInASpecificJvm(EnableGcLoggingAnnotToJvmOptionConverter.INSTANCE)
            .build(EnableGcLogging.class);

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

    static final AnnotationConfig DISPLAY_RSS_FROM_PROCESS_STATUS = new AnnotationConfig.Builder()
            .perfRecorderClass(ProcessStatusRecorder.class)
            .perfIssueVerifier(MeasureRssPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm()
            .build(MeasureRSS.class);

    static final AnnotationConfig MAX_RSS_FROM_PROCESS_STATUS = new AnnotationConfig.Builder()
            .perfRecorderClass(ProcessStatusRecorder.class)
            .perfIssueVerifier(ExpectMaxRssPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm()
            .build(ExpectMaxRSS.class);

}
