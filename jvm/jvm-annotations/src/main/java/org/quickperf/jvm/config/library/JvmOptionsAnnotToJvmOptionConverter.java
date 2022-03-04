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
package org.quickperf.jvm.config.library;

import org.quickperf.WorkingFolder;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;
import org.quickperf.jvm.JvmOptionConverter;

import java.util.List;

class JvmOptionsAnnotToJvmOptionConverter implements AnnotationToJvmOptionConverter<JvmOptions> {

    static final JvmOptionsAnnotToJvmOptionConverter INSTANCE = new JvmOptionsAnnotToJvmOptionConverter();

    private final JvmOptionConverter jvmOptionConverter = JvmOptionConverter.INSTANCE;

    private JvmOptionsAnnotToJvmOptionConverter() {}

    @Override
    public List<JvmOption> convertToJvmOptions(JvmOptions jvmOptions, WorkingFolder workingFolder) {
        return jvmOptionConverter.jvmOptionFrom(jvmOptions);
    }
}
