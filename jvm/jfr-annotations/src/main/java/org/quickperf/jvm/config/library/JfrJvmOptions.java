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

import org.quickperf.jvm.JvmType;
import org.quickperf.jvm.JvmVersion;
import org.quickperf.testlauncher.JvmOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class JfrJvmOptions {

    static final JfrJvmOptions INSTANCE = new JfrJvmOptions();

    private JfrJvmOptions() {}

    private static final List<JvmOption> JVM_OPTIONS = buildJvmOptions();

    private static List<JvmOption> buildJvmOptions() {
        List<JvmOption> jvmOptions = new ArrayList<>(4);
        if(JvmType.isHotSpotJvm() && !JvmVersion.isGreaterThanOrEqualTo11()) {
            jvmOptions.add(new JvmOption("-XX:+UnlockCommercialFeatures"));
        }
        jvmOptions.add(new JvmOption("-XX:+FlightRecorder"));

        jvmOptions.add(new JvmOption("-XX:+UnlockDiagnosticVMOptions"));
        jvmOptions.add(new JvmOption("-XX:+DebugNonSafepoints"));

        return Collections.unmodifiableList(jvmOptions);
    }

    List<JvmOption> getValues() {
        return JVM_OPTIONS;
    }

}
