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

package org.quickperf.jvm.jfr;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.quickperf.WorkingFolder;

import java.io.File;
import java.io.IOException;

class JFREventsLoader {

    public IItemCollection loadJfrEventsFrom(WorkingFolder workingFolder) {

        String jfrFilePath = workingFolder.getPath() + File.separator + "jvm-profiling.jfr";
        File jfrFile = new File(jfrFilePath);
        if(!jfrFile.exists()) {
            String message =
                      "Java Flight Recorder profiling is available for "
                    + System.lineSeparator()
                    + "Oracle JDK >= 1.7u40 and OpenJDK >= 11";
            throw new IllegalStateException(message);
        }

        return load(jfrFilePath);

    }

    private IItemCollection load(String jfrFilePath) {
        try {
            File jfrFile = new File(jfrFilePath);
            IItemCollection jfrEvents = JfrLoaderToolkit.loadEvents(jfrFile);
            System.out.println("[QUICK PERF] JVM was profiled with Java File Recorder (JFR)."
                              + System.lineSeparator()
                              + "The recording file can be found here: "
                              + jfrFilePath + System.lineSeparator()
                              + "You can open it with Java Mission Control (JMC)."
                               )
                              ;
            return jfrEvents;
        } catch (IOException | CouldNotLoadRecordingException e) {
            throw new IllegalStateException("Can't read JFR file.", e);
        }
    }

}
