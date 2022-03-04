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
package org.quickperf.testlauncher;

import org.quickperf.HeapDump;
import org.quickperf.WorkingFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AllJvmOptions {

    private final JvmOption heapDumpOnOomJvmOption = new JvmOption("-XX:+HeapDumpOnOutOfMemoryError");

    private final Set<JvmOption> allJvmOptions;

    private AllJvmOptions(Set<JvmOption> allJvmOptions) {
        this.allJvmOptions = allJvmOptions;
    }

    public static class Builder {

        private final Set<JvmOption> allJvmOptions = new LinkedHashSet<>();

        public Builder addOptions(List<JvmOption> jvmOptions) {
            allJvmOptions.addAll(jvmOptions);
            return this;
        }

        public AllJvmOptions build() {
            return new AllJvmOptions(allJvmOptions);
        }
    }

    public List<String> asStrings(WorkingFolder workingFolder) {

        List<JvmOption> jvmOptions = new ArrayList<>(allJvmOptions);

        jvmOptions.add(heapDumpOnOomJvmOption);

        JvmOption heapDumpPathJvmOption = buildHeapDumpPathJvmParam(workingFolder);
        jvmOptions.add(heapDumpPathJvmOption);

        return toStringList(jvmOptions);

    }

    private List<String> toStringList(List<JvmOption> jvmOptions) {
        List<String> jvmParamsAsStrings = new ArrayList<>();
        for (JvmOption jvmOption : jvmOptions) {
            jvmParamsAsStrings.add(jvmOption.asString());
        }
        return jvmParamsAsStrings;
    }

    private JvmOption buildHeapDumpPathJvmParam(WorkingFolder workingFolder) {
        String workingFolderPath = workingFolder.getPath();
        String heapDumpFilePath = workingFolderPath + File.separator + HeapDump.HEAP_DUMP_NAME;
        return new JvmOption("-XX:HeapDumpPath=" + heapDumpFilePath);
    }

}
