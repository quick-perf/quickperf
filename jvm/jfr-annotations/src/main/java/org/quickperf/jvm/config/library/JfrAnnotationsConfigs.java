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
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.jvm.jfr.annotation.ExpectNoJvmIssue;
import org.quickperf.jvm.jfr.annotation.ProfileJvm;
import org.quickperf.jvm.jfr.annotation.ProfileQuickPerfInTestJvm;
import org.quickperf.jvm.jfr.JfrEventsRecorder;
import org.quickperf.jvm.jmc.value.DisplayJvmProfilingValueVerifier;
import org.quickperf.jvm.jmc.value.JfrEventsMeasureExtractor;
import org.quickperf.jvm.jmcrule.JmcRuleCountMeasureExtractor;
import org.quickperf.jvm.jmcrule.JmcRulesPerfVerifier;
import org.quickperf.testlauncher.AnnotationToJvmOptionConverter;
import org.quickperf.testlauncher.JvmOption;

import java.lang.annotation.Annotation;
import java.util.List;

public class JfrAnnotationsConfigs {

    private JfrAnnotationsConfigs() { }

    static class JfrAnnotationToJvmOptionConverter implements AnnotationToJvmOptionConverter {

        public static final JfrAnnotationToJvmOptionConverter INSTANCE = new JfrAnnotationToJvmOptionConverter();

        private JfrAnnotationToJvmOptionConverter() { }

        @Override
        public List<JvmOption> convertToJvmOptions(Annotation annotation, WorkingFolder workingFolder) {
            return JfrJvmOptions.INSTANCE.getValues();
        }

    }

    static final AnnotationConfig PROFILE_JVM_WITH_JFR = new AnnotationConfig.Builder()
            .perfRecorderClass(JfrEventsRecorder.class)
            .perfMeasureExtractor(JfrEventsMeasureExtractor.INSTANCE)
            .perfIssueVerifier(DisplayJvmProfilingValueVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm(JfrAnnotationToJvmOptionConverter.INSTANCE)
            .build(ProfileJvm.class);

    static final AnnotationConfig CHECK_JVM = new AnnotationConfig.Builder()
            .perfRecorderClass(JfrEventsRecorder.class)
            .perfMeasureExtractor(JmcRuleCountMeasureExtractor.INSTANCE)
            .perfIssueVerifier(JmcRulesPerfVerifier.INSTANCE)
            .testHasToBeLaunchedInASpecificJvm(JfrAnnotationToJvmOptionConverter.INSTANCE)
            .build(ExpectNoJvmIssue.class);

    static final AnnotationConfig PROFILE_QUICK_PERF_WITH_JFR = new AnnotationConfig.Builder()
            .perfRecorderClass(JfrEventsRecorder.class)
            .testHasToBeLaunchedInASpecificJvm(QuickPerfProfilingAnnotToJvmOptionConverter.INSTANCE)
            .build(ProfileQuickPerfInTestJvm.class);

}
