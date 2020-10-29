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

package org.quickperf.jvm.jfr.profiler;

import org.quickperf.jvm.JVM;

public class JdkFlightRecorderProfilerFactory {

    private JdkFlightRecorderProfilerFactory() { }

    public static JvmProfiler getJdkFlightRecorderProfiler() {

        JVM jvm = JVM.INSTANCE;

        JVM.Type jvmType = jvm.type;
        JVM.Version jvmVersion = jvm.version;

        if (jvmType.isHotSpotJvm() && (jvmVersion.is7() || jvmVersion.is8())) {
            return new OracleJdkFlightRecorderProfilerBeforeJava9();
        } else if (
                   (jvmType.isHotSpotJvm() && jvmVersion.isGreaterThanOrEqualTo9())
                || (jvmType.isOpenJdkJvm() && (jvmVersion.is8() || jvmVersion.isGreaterThanOrEqualTo11()))
                  ) {
            return new JdkFlightRecorderProfilerFromJava9();
        }

        return JvmProfiler.NONE;

    }

}
