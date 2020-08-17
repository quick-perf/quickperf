package org.quickperf.jvm.config.library;

import org.quickperf.WorkingFolder;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.jvm.annotations.ExpectNoJvmIssue;
import org.quickperf.jvm.annotations.ProfileJvm;
import org.quickperf.jvm.annotations.ProfileQuickPerfInTestJvm;
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
