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

package org.quickperf.jvm.config.library;

import org.quickperf.jvm.allocation.JvmAllocationUnitFormatter;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.util.ArrayList;
import java.util.List;

class HeapSizeAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<HeapSize> {

    static final HeapSizeAnnotToJvmOptionConverter INSTANCE = new HeapSizeAnnotToJvmOptionConverter();

    private final JvmAllocationUnitFormatter jvmAllocationUnitFormatter = JvmAllocationUnitFormatter.INSTANCE;

    private HeapSizeAnnotToJvmOptionConverter() {}

    @Override
    public List<JvmOption> convertToJvmOptions(HeapSize heapSize) {

        List<JvmOption> jvmOptions = new ArrayList<>(2);
        AllocationUnit allocationUnit = heapSize.unit();
        int heapValue = heapSize.value();

        String allocationUnitAsString = jvmAllocationUnitFormatter.format(allocationUnit);

        String xmsJvmOptionAsString = "-Xms" + heapValue + allocationUnitAsString;
        jvmOptions.add(new JvmOption(xmsJvmOptionAsString));

        String xmxJvmOptionAsString = "-Xmx" + heapValue + allocationUnitAsString;
        jvmOptions.add(new JvmOption(xmxJvmOptionAsString));

        return jvmOptions;
    }
}
