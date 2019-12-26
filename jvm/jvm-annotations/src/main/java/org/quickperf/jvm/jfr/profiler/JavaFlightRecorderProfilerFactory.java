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

package org.quickperf.jvm.jfr.profiler;

import org.quickperf.jvm.JvmType;
import org.quickperf.jvm.JvmVersion;

public class JavaFlightRecorderProfilerFactory {

    private JavaFlightRecorderProfilerFactory() { }

    public static JvmProfiler getJavaFlightRecorderProfiler() {

        if (JvmType.isHotSpotJvm() && (JvmVersion.is7() || JvmVersion.is8())) {
            return new JavaFlightRecorderProfilerBeforeJava9();
        } else if ((JvmType.isHotSpotJvm() && JvmVersion.isGreaterThanOrEqualTo9())
                || (JvmType.isOpenJdkJvm() && JvmVersion.isGreaterThanOrEqualTo11())
        ) {
            return new JavaFlightRecorderProfilerFromJava9();
        }

        return JvmProfiler.NONE;

    }

}
