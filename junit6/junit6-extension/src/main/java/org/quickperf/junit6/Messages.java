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

import java.lang.annotation.Annotation;
import java.util.List;

final class Messages {

    private static final String PREFIX = "[QUICK PERF] ";

    private static final String EXTENSION = "the QuickPerf JUnit 6 extension (quick-perf-junit6)";

    private Messages() { }

    static String jvmAnnotationsNotApplied(List<Annotation> annotations) {
        boolean plural = annotations.size() > 1;
        return PREFIX + namesOf(annotations) + (plural ? " need" : " needs")
             + " QuickPerf to start a dedicated JVM for the test, but " + EXTENSION
             + " executes every test in the JVM running JUnit: it cannot apply " + (plural ? "them" : "it") + "."
             + System.lineSeparator()
             + (plural ? "Remove them" : "Remove it")
             + ". The QuickPerf extensions for JUnit 4, JUnit 5 and TestNG start a dedicated JVM for such a test.";
    }

    static String globalJvmAnnotationsNotApplied(List<Annotation> annotations, String globalAnnotationsClassName) {
        boolean plural = annotations.size() > 1;
        String ofClass = globalAnnotationsClassName == null ? "" : " of " + globalAnnotationsClassName;
        return PREFIX + "The QuickPerf JUnit 6 extension (quick-perf-junit6) does not apply the global "
             + (plural ? "annotations " : "annotation ") + namesOf(annotations) + ofClass + ": "
             + (plural ? "they need" : "it needs")
             + " QuickPerf to start a dedicated JVM for each test, but the extension executes every test in the JVM"
             + " running JUnit.";
    }

    static String notExecutedThroughQuickPerf() {
        return PREFIX + "The QuickPerf JUnit 6 extension (quick-perf-junit6) did not execute the test method, so it"
             + " checked no performance property: an extension registered before it executed or skipped the test"
             + " method itself."
             + System.lineSeparator()
             + "Register QuickPerf first: declare @QuickPerfTest before the other extension annotations, or enable the"
             + " autodetection of extensions.";
    }

    static String bothQuickPerfExtensionsRegistered() {
        return PREFIX + "The QuickPerf JUnit 5 extension (quick-perf-junit5) and the QuickPerf JUnit 6 extension"
             + " (quick-perf-junit6) are both registered: their SQL recordings would mix."
             + System.lineSeparator()
             + "With JUnit 6, keep only quick-perf-junit6, with org.quickperf.junit6.QuickPerfTest. When both are"
             + " auto-detected (junit.jupiter.extensions.autodetection.enabled=true), remove quick-perf-junit5 from the"
             + " test class path, or exclude it with"
             + " junit.jupiter.extensions.autodetection.exclude=org.quickperf.junit5.*";
    }

    /**
     * Lists the annotations as in an English sentence: the simple name of each annotation, prefixed with @, separated
     * by commas, and "and" before the last one, without a comma before "and". For example: "@HeapSize",
     * "@HeapSize and @ProfileJvm", or "@HeapSize, @UseGC and @ProfileJvm".
     *
     * @param annotations at least one annotation
     */
    private static String namesOf(List<Annotation> annotations) {
        List<String> names = annotations.stream()
                                        .map(annotation -> "@" + annotation.annotationType().getSimpleName())
                                        .toList();
        if (names.size() == 1) {
            return names.get(0);
        }
        String allButTheLast = String.join(", ", names.subList(0, names.size() - 1));
        String last = names.get(names.size() - 1);
        return allButTheLast + " and " + last;
    }

}
