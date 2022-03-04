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
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.jvm.jfr.profiler;

import javax.management.*;
import java.io.File;
import java.lang.management.ManagementFactory;

class JdkFlightRecorderProfilerFromJava9 implements JvmProfiler {

    private final MBeanServer mBeanServer;

    private final ObjectName objectName;

    JdkFlightRecorderProfilerFromJava9() {
        this.mBeanServer = ManagementFactory.getPlatformMBeanServer();
        this.objectName = getObjectName();
    }

    private ObjectName getObjectName() {
        try {
            return new ObjectName("jdk.management.jfr:type=FlightRecorder");
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void startProfiling(String workingFolderPath) {
        try {
            Long recordingId = (Long) mBeanServer.invoke(objectName, "newRecording", new Object[]{}, new String[0]);

            Object[] setConfigArgs = new Object[]{recordingId, "profile"};
            mBeanServer.invoke(objectName, "setPredefinedConfiguration", setConfigArgs, new String[]{long.class.getName(), String.class.getName()});

            Object[] startRecordingArgs = new Object[]{recordingId};
            mBeanServer.invoke(objectName, "startRecording", startRecordingArgs, new String[]{long.class.getName()});
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void stopProfiling(String workingFolderPath) {
        String jfrPath = workingFolderPath + File.separator + FlightRecorder.FILE_NAME;
        try {

            long recordingId = (long) mBeanServer.invoke(objectName
                                                       , "takeSnapshot"
                                                       , new Object[]{}
                                                       , new String[0]
                                                        );

            Object[] copyToArgs = new Object[]{recordingId, jfrPath};
            mBeanServer.invoke(objectName, "copyTo", copyToArgs, new String[]{long.class.getName(), String.class.getName()});

        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new IllegalStateException(e);
        }

    }

}
