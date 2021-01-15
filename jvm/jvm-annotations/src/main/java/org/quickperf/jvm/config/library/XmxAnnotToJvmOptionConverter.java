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

import org.quickperf.WorkingFolder;
import org.quickperf.jvm.allocation.JvmAllocationUnitFormatter;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.Xmx;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.util.Collections;
import java.util.List;

class XmxAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<Xmx> {

    static final XmxAnnotToJvmOptionConverter INSTANCE = new XmxAnnotToJvmOptionConverter();

    private XmxAnnotToJvmOptionConverter() { }

    private final JvmAllocationUnitFormatter jvmAllocationUnitFormatter = JvmAllocationUnitFormatter.INSTANCE;

    @Override
    public List<JvmOption> convertToJvmOptions(Xmx xmx, WorkingFolder workingFolder) {
        AllocationUnit allocationUnit = xmx.unit();
        String jvmParamOption = "-Xmx" + xmx.value() + jvmAllocationUnitFormatter.format(allocationUnit);
        JvmOption jvmOption = new JvmOption(jvmParamOption);
        return Collections.singletonList(jvmOption);
    }

}
