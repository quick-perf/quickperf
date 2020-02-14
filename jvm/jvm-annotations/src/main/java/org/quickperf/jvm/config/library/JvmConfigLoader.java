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

import org.quickperf.config.library.QuickPerfConfigLoader;
import org.quickperf.RecorderExecutionOrder;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder;
import org.quickperf.jvm.jfr.JfrEventsRecorder;
import org.quickperf.jvm.rss.ProcessStatusRecorder;

import java.util.Arrays;
import java.util.Collection;

public class JvmConfigLoader implements QuickPerfConfigLoader {

    @Override
    public Collection<AnnotationConfig> loadAnnotationConfigs() {
        return Arrays.asList(
                  JvmAnnotationsConfigs.XMX
                , JvmAnnotationsConfigs.XMS
                , JvmAnnotationsConfigs.HEAP_SIZE
                , JvmAnnotationsConfigs.JVM_OPTIONS
                , JvmAnnotationsConfigs.DISPLAY_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.MAX_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.NO_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.CHECK_JVM
                , JvmAnnotationsConfigs.PROFILE_QUICK_PERF_WITH_JMC
                , JvmAnnotationsConfigs.PROFILE_JVM_WITH_JFR
                , JvmAnnotationsConfigs.DISPLAY_RSS_FROM_PROCESS_STATUS
                , JvmAnnotationsConfigs.MAX_RSS_FROM_PROCESS_STATUS
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersBeforeTestMethod() {
        return Arrays.asList(
                  new RecorderExecutionOrder(ProcessStatusRecorder.class, 5070)
                , new RecorderExecutionOrder(JfrEventsRecorder.class, 6000)
                , new RecorderExecutionOrder(ByteWatcherRecorder.class, 6030)
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersAfterTestMethod() {
        return Arrays.asList(
                  new RecorderExecutionOrder(ByteWatcherRecorder.class, 3000)
                , new RecorderExecutionOrder(JfrEventsRecorder.class, 3030)
                , new RecorderExecutionOrder(ProcessStatusRecorder.class, 3060)
        );
    }

}
