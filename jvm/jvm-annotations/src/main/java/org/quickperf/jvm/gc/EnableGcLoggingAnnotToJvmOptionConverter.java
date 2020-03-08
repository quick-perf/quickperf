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

package org.quickperf.jvm.gc;

import org.quickperf.WorkingFolder;
import org.quickperf.jvm.JvmVersion;
import org.quickperf.jvm.annotations.EnableGcLogging;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class EnableGcLoggingAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<EnableGcLogging> {

    public static EnableGcLoggingAnnotToJvmOptionConverter INSTANCE = new EnableGcLoggingAnnotToJvmOptionConverter();

    private EnableGcLoggingAnnotToJvmOptionConverter() { }

    @Override
    public List<JvmOption> convertToJvmOptions(EnableGcLogging annotation, WorkingFolder workingFolder) {

        String gcLogPath = workingFolder.getPath() + File.separator + "gc.log";
        System.out.println("GC log file: " + gcLogPath);

        if (JvmVersion.isGreaterThanOrEqualTo9()) {
            return Arrays.asList(new JvmOption("-Xlog:gc*,gc+age=trace:file=\\\"" + gcLogPath + "\\\":tags,uptime,time"));
        }

        return Arrays.asList( new JvmOption("-XX:+PrintGCDetails")
                            , new JvmOption("-XX:+PrintGCTimeStamps")
                            , new JvmOption("-XX:+PrintGCDateStamps")
                            , new JvmOption("-XX:+PrintTenuringDistribution")
                            , new JvmOption("-Xloggc:" + gcLogPath)
                            );

    }

}
