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

import javax.management.*;
import java.io.File;
import java.lang.management.ManagementFactory;

class OracleJdkFlightRecorderProfilerBeforeJava9 implements JvmProfiler {

    private final String name = "1";

    private final MBeanServer mBeanServer;

    private final ObjectName objectName;

    OracleJdkFlightRecorderProfilerBeforeJava9() {
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
        this.objectName = getObjectName();
    }

    private ObjectName getObjectName() {
        try {
            return new ObjectName("com.sun.management:type=DiagnosticCommand");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void startProfiling(String workingFolderPath) {
        try {
            Object[] jfrStartArgs = new Object[]{new String[]{"name=" + name, "settings=" + "profile.jfc"}};
            mBeanServer.invoke(objectName, "jfrStart", jfrStartArgs, new String[]{String[].class.getName()});
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stopProfiling(String workingFolderPath) {
        try {
            Object[] jfrStopArgs = new Object[]{new String[]{"name=" + name, "filename=" + workingFolderPath + File.separator + FlightRecorder.FILE_NAME, "compress=" + false}};
            mBeanServer.invoke(objectName, "jfrStop", jfrStopArgs, new String[]{String[].class.getName()});
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new IllegalStateException(e);
        }
    }

}
