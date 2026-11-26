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
package org.quickperf.junit6;

import org.junit.jupiter.api.Test;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesTest {

    @ExpectSelect
    @ExpectInsert
    @DisplaySql
    private static void annotated_method() { }

    private static final Annotation FIRST = annotationOfType(ExpectSelect.class);

    private static final Annotation SECOND = annotationOfType(ExpectInsert.class);

    private static final Annotation THIRD = annotationOfType(DisplaySql.class);

    private static Annotation annotationOfType(Class<? extends Annotation> annotationType) {
        try {
            return MessagesTest.class.getDeclaredMethod("annotated_method").getAnnotation(annotationType);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String NEW_LINE = System.lineSeparator();

    @Test
    void should_name_one_annotation_needing_a_dedicated_jvm() {
        assertThat(Messages.jvmAnnotationsNotApplied(List.of(FIRST))).isEqualTo(
                "[QUICK PERF] @ExpectSelect needs QuickPerf to start a dedicated JVM for the test, but the QuickPerf"
              + " JUnit 6 extension (quick-perf-junit6) executes every test in the JVM running JUnit: it cannot apply"
              + " it." + NEW_LINE
              + "Remove it. The QuickPerf extensions for JUnit 4, JUnit 5 and TestNG start a dedicated JVM for such a"
              + " test.");
    }

    @Test
    void should_name_two_annotations_needing_a_dedicated_jvm() {
        assertThat(Messages.jvmAnnotationsNotApplied(List.of(FIRST, SECOND))).isEqualTo(
                "[QUICK PERF] @ExpectSelect and @ExpectInsert need QuickPerf to start a dedicated JVM for the test, but"
              + " the QuickPerf JUnit 6 extension (quick-perf-junit6) executes every test in the JVM running JUnit: it"
              + " cannot apply them." + NEW_LINE
              + "Remove them. The QuickPerf extensions for JUnit 4, JUnit 5 and TestNG start a dedicated JVM for such"
              + " a test.");
    }

    @Test
    void should_name_three_annotations_needing_a_dedicated_jvm() {
        assertThat(Messages.jvmAnnotationsNotApplied(List.of(FIRST, SECOND, THIRD)))
                .startsWith("[QUICK PERF] @ExpectSelect, @ExpectInsert and @DisplaySql need QuickPerf to start a"
                          + " dedicated JVM for the test");
    }

    @Test
    void should_name_one_global_annotation_needing_a_dedicated_jvm() {
        assertThat(Messages.globalJvmAnnotationsNotApplied(List.of(FIRST), "org.quickperf.QuickPerfConfiguration"))
                .isEqualTo("[QUICK PERF] The QuickPerf JUnit 6 extension (quick-perf-junit6) does not apply the global"
                         + " annotation @ExpectSelect of org.quickperf.QuickPerfConfiguration: it needs QuickPerf to"
                         + " start a dedicated JVM for each test, but the extension executes every test in the JVM"
                         + " running JUnit.");
    }

    @Test
    void should_name_two_global_annotations_needing_a_dedicated_jvm() {
        assertThat(Messages.globalJvmAnnotationsNotApplied(List.of(FIRST, SECOND), "org.quickperf.QuickPerfConfiguration"))
                .isEqualTo("[QUICK PERF] The QuickPerf JUnit 6 extension (quick-perf-junit6) does not apply the global"
                         + " annotations @ExpectSelect and @ExpectInsert of org.quickperf.QuickPerfConfiguration: they"
                         + " need QuickPerf to start a dedicated JVM for each test, but the extension executes every"
                         + " test in the JVM running JUnit.");
    }

}
