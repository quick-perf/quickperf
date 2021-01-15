/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.reporter;

import org.quickperf.AnnotationFormatter;
import org.quickperf.AnnotationsExtractor;
import org.quickperf.RecorderExecutionOrder;
import org.quickperf.annotation.DisplayAppliedAnnotations;
import org.quickperf.config.SpecifiableGlobalAnnotations;
import org.quickperf.config.library.QuickPerfConfigLoader;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

class ConsoleReporter {

    static final ConsoleReporter INSTANCE = new ConsoleReporter();

    private ConsoleReporter() {}

    private static final AnnotationFormatter ANNOTATION_FORMATTER = AnnotationFormatter.INSTANCE;

    void displayQuickPerfDebugInfos(List<String> jvmOptions) {

        System.out.println();

        System.out.println("[QUICK PERF] [DEBUG] " + System.lineSeparator());

        printJvmOptions(jvmOptions);

        printExecutionOrders();

    }

    private void printJvmOptions(List<String> jvmOptions) {
        if(!jvmOptions.isEmpty()) {
            System.out.println("JVM OPTIONS");

            for (String jvmOption: jvmOptions) {
                System.out.println(jvmOption);
            }
            System.out.println();
        }
    }

    private void printExecutionOrders() {
        ServiceLoader<QuickPerfConfigLoader> serviceLoader = ServiceLoader.load(QuickPerfConfigLoader.class);
        Iterator<QuickPerfConfigLoader> serviceIterator = serviceLoader.iterator();

        List<RecorderExecutionOrder> executionOrderListBefore = new ArrayList<>();
        List<RecorderExecutionOrder> executionOrderListAfter = new ArrayList<>();

        while(serviceIterator.hasNext()) {
            QuickPerfConfigLoader quickPerfConfig = serviceIterator.next();
            Collection<RecorderExecutionOrder> recorderExecutionOrdersBefore = quickPerfConfig.loadRecorderExecutionOrdersBeforeTestMethod();
            Collection<RecorderExecutionOrder> recorderExecutionOrdersAfter = quickPerfConfig.loadRecorderExecutionOrdersAfterTestMethod();

            executionOrderListBefore.addAll(recorderExecutionOrdersBefore);
            executionOrderListAfter.addAll(recorderExecutionOrdersAfter);
        }

        System.out.println("PRIORITY OF RECORDERS EXECUTED BEFORE TEST METHOD");
        printExecutionOrders(executionOrderListBefore);

        System.out.println();
        System.out.println("PRIORITY OF RECORDERS EXECUTED AFTER TEST METHOD");
        printExecutionOrders(executionOrderListAfter);
    }

    private void printExecutionOrders(List<RecorderExecutionOrder> executionOrderList) {
        Collections.sort(executionOrderList);
        System.out.println("----" + " | -----------------------------------------");
        System.out.println("Prio" + " | Recorder");
        System.out.println("----" + " | -----------------------------------------");
        for (RecorderExecutionOrder executionOrder : executionOrderList) {
            System.out.println(executionOrder.getExecutionPriority() + " | "
                    + executionOrder.getPerfRecorderClass().getName());
        }
    }

    private List<Annotation> removeDisplayAppliedAnnotations(Annotation[] perfAnnotations) {
        List<Annotation> perfAnnotationsWithoutDisplayAppliedAnnotations = new ArrayList<>(perfAnnotations.length - 1);
        for (Annotation perfAnnotation : perfAnnotations) {
            if (!perfAnnotation.annotationType().equals(DisplayAppliedAnnotations.class)) {
                perfAnnotationsWithoutDisplayAppliedAnnotations.add(perfAnnotation);
            }
        }
        return perfAnnotationsWithoutDisplayAppliedAnnotations;
    }

    private String buildPerfAnnotationAsString(List<Annotation> perfAnnotations) {
        String perfAnnotationsAsString = "";
        for (int i = 0; i < perfAnnotations.size(); i++) {
            Annotation perfAnnotation = perfAnnotations.get(i);
            if(i != 0) {
                perfAnnotationsAsString += ", ";
            }
            perfAnnotationsAsString += ANNOTATION_FORMATTER.format(perfAnnotation);
        }
        return perfAnnotationsAsString;
    }

    void displayQuickPerfAnnotations(Annotation[] perfAnnotations) {

        List<Annotation> perfAnnotationsWithoutDisplayAppliedAnnotations =
                removeDisplayAppliedAnnotations(perfAnnotations);

        String perfAnnotationsAsString = buildPerfAnnotationAsString(perfAnnotationsWithoutDisplayAppliedAnnotations);

        System.out.println("[QUICK PERF] Applied annotations: " + perfAnnotationsAsString);

        SpecifiableGlobalAnnotations classSpecifyingGlobalAnnotations =
                AnnotationsExtractor.INSTANCE.classSpecifyingGlobalAnnotation();
        if(classSpecifyingGlobalAnnotations != null) {
            String nameOfClassSpecifyingGlobalAnnotation = classSpecifyingGlobalAnnotations.getClass().getCanonicalName();
            System.out.println("             Class specifying global annotations: " + nameOfClassSpecifyingGlobalAnnotation);
        }
    }

}
