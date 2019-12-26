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

package org.quickperf.jvm;

import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.testlauncher.JvmOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JvmOptionConverter {

    public static final JvmOptionConverter INSTANCE = new JvmOptionConverter();

    private JvmOptionConverter() {}

    public List<JvmOption> jvmOptionFrom(JvmOptions jvmOptions) {
        if (!jvmOptionsExist(jvmOptions)) {
            return Collections.emptyList();
        }
        String jvmOptionsAsAString = jvmOptions.value();
        List<String> jvmOptionsAsStrings = extractJvmOptionsAsStrings(jvmOptionsAsAString);
        return buildJvmOptionsAsObjects(jvmOptionsAsStrings);
    }

    private boolean jvmOptionsExist(JvmOptions jvmOptions) {
        String jvmOptionsAsAString = jvmOptions.value();
        return jvmOptions != null && !jvmOptionsAsAString.isEmpty();
    }

    private List<String> extractJvmOptionsAsStrings(String jvmOptionsAsAString) {
        List<String> jvmOptionsAsStrings = new ArrayList<>();
        String[] splittedJvmOptions = jvmOptionsAsAString.split("\\s");
        for (String splittedJvmOption : splittedJvmOptions) {
            if(!splittedJvmOption.isEmpty()) {
                jvmOptionsAsStrings.add(splittedJvmOption);
            }
        }
        return jvmOptionsAsStrings;
    }

    private List<JvmOption> buildJvmOptionsAsObjects(List<String> jvmOptionsAsStrings) {
        List<JvmOption> jvmOptionsAsObjects = new ArrayList<>();
        for (String splittedJvmOption : jvmOptionsAsStrings) {
            JvmOption jvmOption = new JvmOption(splittedJvmOption);
            jvmOptionsAsObjects.add(jvmOption);
        }
        return jvmOptionsAsObjects;
    }

}
