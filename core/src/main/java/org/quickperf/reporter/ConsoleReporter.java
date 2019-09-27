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

public final class ConsoleReporter {

    private static final AnnotationFormatter ANNOTATION_FORMATTER = AnnotationFormatter.INSTANCE;

    private ConsoleReporter (){
        //utility class pattern
    }

    public static void displayQuickPerfDebugInfos() {

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

        System.out.println("[QUICK PERF DEBUG] " + System.lineSeparator());

        System.out.println("PRIORITY OF RECORDERS EXECUTED BEFORE TEST METHOD");
        printExecutionOrders(executionOrderListBefore);

        System.out.println();
        System.out.println("PRIORITY OF RECORDERS EXECUTED AFTER TEST METHOD");
        printExecutionOrders(executionOrderListAfter);
    }

    public static void displayQuickPerfAnnotations(Annotation[] perfAnnotations) {

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

    private static void printExecutionOrders(List<RecorderExecutionOrder> executionOrderList) {
        Collections.sort(executionOrderList);
        System.out.println("----" + " | -----------------------------------------");
        System.out.println("Prio" + " | Recorder");
        System.out.println("----" + " | -----------------------------------------");
        for (RecorderExecutionOrder executionOrder : executionOrderList) {
            System.out.println(executionOrder.getExecutionPriority() + " | "
                    + executionOrder.getPerfRecorderClass().getName());
        }
    }

    private static List<Annotation> removeDisplayAppliedAnnotations(Annotation[] perfAnnotations) {
        List<Annotation> perfAnnotationsWithoutDisplayAppliedAnnotations = new ArrayList<>(perfAnnotations.length - 1);
        for (Annotation perfAnnotation : perfAnnotations) {
            if (!perfAnnotation.annotationType().equals(DisplayAppliedAnnotations.class)) {
                perfAnnotationsWithoutDisplayAppliedAnnotations.add(perfAnnotation);
            }
        }
        return perfAnnotationsWithoutDisplayAppliedAnnotations;
    }

    private static String buildPerfAnnotationAsString(List<Annotation> perfAnnotations) {
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
}
