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

import org.quickperf.jvm.allocation.JvmAllocationUnitFormatter;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;
import org.quickperf.jvm.annotations.Xms;

import java.util.Collections;
import java.util.List;

class XmsAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<Xms> {

    static final XmsAnnotToJvmOptionConverter INSTANCE = new XmsAnnotToJvmOptionConverter();

    private XmsAnnotToJvmOptionConverter() {}

    private final JvmAllocationUnitFormatter jvmAllocationUnitFormatter = JvmAllocationUnitFormatter.INSTANCE;

    @Override
    public List<JvmOption> convertToJvmOptions(Xms xms) {
        AllocationUnit allocationUnit = xms.unit();
        String jvmOptionAsString = "-Xms" + xms.value() + jvmAllocationUnitFormatter.format(allocationUnit);
        JvmOption jvmOption = new JvmOption(jvmOptionAsString);
        return Collections.singletonList(jvmOption);
    }

}
