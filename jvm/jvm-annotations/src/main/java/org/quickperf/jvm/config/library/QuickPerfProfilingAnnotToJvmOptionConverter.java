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

import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;
import org.quickperf.jvm.annotations.ProfileQuickPerfInTestJvm;

import java.util.ArrayList;
import java.util.List;

class QuickPerfProfilingAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<ProfileQuickPerfInTestJvm> {

    static final AnnotationToJvmOptionConverter INSTANCE = new QuickPerfProfilingAnnotToJvmOptionConverter();

    private static final List<JvmOption> PROFILING_OPTIONS = buildProfilingOptions();

    private static List<JvmOption> buildProfilingOptions() {

        List<JvmOption> jfrJvmOptions = JfrJvmOptions.INSTANCE.getValues();
        List<JvmOption> jvmOptions = new ArrayList<>(jfrJvmOptions);

        JvmOption flightRecorderOptions = new JvmOption("-XX:FlightRecorderOptions=defaultrecording=true,disk=true,settings=profile,dumponexit=true,dumponexitpath=dumponexit.jfr");
        jvmOptions.add(flightRecorderOptions);

        return jvmOptions;
    }

    @Override
    public List<JvmOption> convertToJvmOptions(ProfileQuickPerfInTestJvm annotation) {
        return PROFILING_OPTIONS;
    }

}
