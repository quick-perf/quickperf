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

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.quickperf.AnnotationsExtractor;
import org.quickperf.config.SpecifiableGlobalAnnotations;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The annotations needing a specific JVM, which the extension never applies. On the test method or its class, they fail
 * the test: the developer expects them to be checked. In the global annotations, which apply to every test of a module,
 * they are ignored with a warning.
 */
final class JvmAnnotations {

    private static final String GLOBAL_WARNING_KEY = "global-jvm-annotations-warning";

    // One console line per JVM: with Maven Surefire, a launcher execution can cover a single test class
    private static final AtomicBoolean GLOBAL_WARNING_PRINTED = new AtomicBoolean();

    private JvmAnnotations() { }

    /**
     * To call before building the test execution context: building it can have side effects.
     *
     * @throws ExtensionConfigurationException if the test method or its class has an annotation needing a specific JVM
     */
    static void failOrWarn(PerfAnnotations perfAnnotations, ExtensionContext context) {

        List<Annotation> testAnnotations = perfAnnotations.testAnnotationsNeedingASpecificJvm();
        if (!testAnnotations.isEmpty()) {
            // An error, not an AssertionError: nothing was measured
            throw new ExtensionConfigurationException(Messages.jvmAnnotationsNotApplied(testAnnotations));
        }

        List<Annotation> globalAnnotations = perfAnnotations.globalAnnotationsNeedingASpecificJvm();
        if (!globalAnnotations.isEmpty()) {
            warn(Messages.globalJvmAnnotationsNotApplied(globalAnnotations, GlobalAnnotationsClass.NAME), context);
        }

    }

    // Found once, for the first warning: classSpecifyingGlobalAnnotation() scans the class path and instantiates the class
    private static final class GlobalAnnotationsClass {

        // Null if there is none, or if the scan fails: the warning then names no class
        static final String NAME = find();

        private static String find() {
            try {
                SpecifiableGlobalAnnotations globalAnnotations = AnnotationsExtractor.INSTANCE.classSpecifyingGlobalAnnotation();
                return globalAnnotations == null ? null : globalAnnotations.getClass().getName();
            } catch (RuntimeException e) {
                return null;
            }
        }

    }

    private static void warn(String warning, ExtensionContext context) {
        // Maven Surefire neither prints the report entries nor writes them to its reports
        printOnce(warning, System.err, GLOBAL_WARNING_PRINTED);
        // One report entry per launcher execution, for the IDEs. A static field would be shared by the launcher
        // executions of a JVM.
        AtomicBoolean published = context.getRoot()
                                         .getStore(QuickPerfTestExtension.NAMESPACE)
                                         .computeIfAbsent(GLOBAL_WARNING_KEY, key -> new AtomicBoolean(), AtomicBoolean.class);
        if (published.compareAndSet(false, true)) {
            context.publishReportEntry("QuickPerf", warning);
        }
    }

    // One call: the line does not interleave with the output of tests executed concurrently
    static void printOnce(String warning, PrintStream output, AtomicBoolean printed) {
        if (printed.compareAndSet(false, true)) {
            output.println(warning);
        }
    }

}
