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

package org.quickperf.jvm.config.library;

import org.quickperf.RecorderExecutionOrder;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.QuickPerfConfigLoader;
import org.quickperf.jvm.allocation.bytewatcher.ByteWatcherRecorder;
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
                , JvmAnnotationsConfigs.USE_GC
                , JvmAnnotationsConfigs.ENABLE_GC_LOGGING
                , JvmAnnotationsConfigs.DISPLAY_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.MAX_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.NO_ALLOCATION_BY_BYTE_WATCHER
                , JvmAnnotationsConfigs.DISPLAY_RSS_FROM_PROCESS_STATUS
                , JvmAnnotationsConfigs.MAX_RSS_FROM_PROCESS_STATUS
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersBeforeTestMethod() {
        return Arrays.asList(
                  new RecorderExecutionOrder(ProcessStatusRecorder.class, 5070)
                , new RecorderExecutionOrder(ByteWatcherRecorder.class, 6030)
        );
    }

    @Override
    public Collection<RecorderExecutionOrder> loadRecorderExecutionOrdersAfterTestMethod() {
        return Arrays.asList(
                  new RecorderExecutionOrder(ByteWatcherRecorder.class, 3000)
                , new RecorderExecutionOrder(ProcessStatusRecorder.class, 3060)
        );
    }

}
