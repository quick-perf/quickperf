package org.quickperf.junit5;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.quickperf.*;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.perfrecording.PerfRecord;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.perfrecording.ViewablePerfRecordIfPerfIssue;
import org.quickperf.reporter.ConsoleReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickPerfTestExtension implements BeforeEachCallback, InvocationInterceptor {

    private final QuickPerfConfigs quickPerfConfigs =  QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();
    private final IssueThrower issueThrower = IssueThrower.INSTANCE;
    private final NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;
    private final JUnit5FailuresRepository jUnit5FailuresRepository = JUnit5FailuresRepository.INSTANCE;

    private TestExecutionContext testExecutionContext;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs, extensionContext.getRequiredTestMethod());
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation,
                                    ReflectiveInvocationContext<Method> invocationContext,
                                    ExtensionContext extensionContext) throws Throwable {
        List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod = testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod();
        List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();

        exePerfInstrumentBeforeTestMethod(perfRecordersToExecuteBeforeTestMethod);
        Throwable businessThrowable = null;
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            newJvmTestLauncher.run( invocationContext.getExecutable()
                    , testExecutionContext.getWorkingFolder()
                    , testExecutionContext.getJvmOptions()
                    , QuickPerfJunit5Core.class);
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            businessThrowable = jUnit5FailuresRepository.find(workingFolder);
        }
        else {
            try{
                invocation.proceed();
            }
            catch (Throwable throwable){
                businessThrowable = throwable;
            }
        }

        exePerfInstrumentAfterTestMethod(perfRecordersToExecuteAfterTestMethod);

        Map<Annotation, PerfRecord> perfRecordByAnnotation
                = buildPerfRecordByAnnotation(quickPerfConfigs.getTestAnnotationConfigs());

        Map<Annotation, PerfIssue> perfIssuesByAnnotation
                = evaluatePerfIssuesByAnnotation(perfRecordByAnnotation);

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat
                = perfIssuesToFormatGroup(perfRecordByAnnotation, perfIssuesByAnnotation);

        cleanResources();

        if(testExecutionContext.areQuickPerfAnnotationsToBeDisplayed()) {
            ConsoleReporter.displayQuickPerfAnnotations(testExecutionContext.getPerfAnnotations());
        }

        if (testExecutionContext.isQuickPerfDebugMode()) {
            ConsoleReporter.displayQuickPerfDebugInfos();
        }

        issueThrower.throwIfNecessary(businessThrowable, groupOfPerfIssuesToFormat);
    }

    private void exePerfInstrumentBeforeTestMethod(List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod) {
        for (int i = 0; i < perfRecordersToExecuteBeforeTestMethod.size(); i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteBeforeTestMethod.get(i);
            recordablePerformance.startRecording(testExecutionContext);
        }
    }

    private void exePerfInstrumentAfterTestMethod(List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod) {
        for (int i = 0; i < perfRecordersToExecuteAfterTestMethod.size() ; i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteAfterTestMethod.get(i);
            recordablePerformance.stopRecording(testExecutionContext);
        }
    }

    private Map<Annotation, PerfIssue> evaluatePerfIssuesByAnnotation(Map<Annotation, PerfRecord> perfRecordByAnnotation) {
        Map<Annotation, PerfMeasure> perfMeasureByAnnotation
                = extractPerfMeasureByAnnotation(quickPerfConfigs.getTestAnnotationConfigs(), perfRecordByAnnotation);
        return evaluatePerfIssuesByAnnotation(perfMeasureByAnnotation
                , quickPerfConfigs.getTestAnnotationConfigs());
    }

    private Map<Annotation, PerfRecord> buildPerfRecordByAnnotation(SetOfAnnotationConfigs testAnnotationConfigs) {
        Map<Annotation, PerfRecord> perfRecordByAnnotation =new HashMap<>();
        Map<Class<? extends RecordablePerformance>, RecordablePerformance> perfRecorderByPerfRecorderClass = buildPerfRecorderInstanceByPerfRecorderClass();
        for (Annotation annotation : testExecutionContext.getPerfAnnotations()) {
            Class<? extends RecordablePerformance> perfRecorderClass = testAnnotationConfigs.retrievePerfRecorderClassFor(annotation);
            RecordablePerformance perfRecorder = perfRecorderByPerfRecorderClass.get(perfRecorderClass);
            if (perfRecorder != null) {
                PerfRecord perfRecord = findPerfRecord(perfRecorder);
                perfRecordByAnnotation.put(annotation, perfRecord);

            }
        }
        return perfRecordByAnnotation;
    }

    private PerfRecord findPerfRecord(RecordablePerformance perfRecorder) {
        try {
            return perfRecorder.findRecord(testExecutionContext);
        } catch (Exception e) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            Throwable throwableFromTestJvm = jUnit5FailuresRepository.find(workingFolder);
            if(throwableFromTestJvm != null) {
                e.addSuppressed(throwableFromTestJvm);
            }
            throw e;
        }
    }

    private Collection<PerfIssuesToFormat> perfIssuesToFormatGroup(
            Map<Annotation, PerfRecord> perfRecordByAnnotation
            , Map<Annotation, PerfIssue> perfIssuesByAnnotation) {
        List<PerfIssuesToFormat> perfIssuesToFormatGroup = new ArrayList<>();
        Map<PerfRecord, List<PerfIssue>> perfIssuesByPerfRecord = buildPerfIssuesByPerfRecord(perfRecordByAnnotation, perfIssuesByAnnotation);
        for (PerfRecord perfRecord : perfIssuesByPerfRecord.keySet()) {
            List<PerfIssue> perfIssues = perfIssuesByPerfRecord.get(perfRecord);
            PerfIssuesFormat perfIssuesFormat = retrievePerfIssuesFormat(perfRecord);
            PerfIssuesToFormat perfIssuesToFormat = new PerfIssuesToFormat(perfIssues, perfIssuesFormat);
            perfIssuesToFormatGroup.add(perfIssuesToFormat);
        }
        return perfIssuesToFormatGroup;
    }

    private PerfIssuesFormat retrievePerfIssuesFormat(PerfRecord perfRecord) {
        if (perfRecord instanceof PerfIssuesFormat) {
            return (PerfIssuesFormat) perfRecord;
        }
        return ViewablePerfRecordIfPerfIssue.STANDARD;
    }

    private Map<PerfRecord, List<PerfIssue>> buildPerfIssuesByPerfRecord(Map<Annotation, PerfRecord> perfRecordByAnnotation, Map<Annotation, PerfIssue> perfIssuesByAnnotation) {
        Map<PerfRecord, List<PerfIssue>> perfIssuesByPerfRecord = new HashMap<>();
        for (Annotation annotation : perfRecordByAnnotation.keySet()) {

            PerfRecord perfRecord = perfRecordByAnnotation.get(annotation);

            List<PerfIssue> perfIssues = perfIssuesByPerfRecord.get(perfRecord);
            if(perfIssues == null) {
                perfIssues = new ArrayList<>();
            }
            PerfIssue perfIssue = perfIssuesByAnnotation.get(annotation);
            if(perfIssue != null) {
                perfIssues.add(perfIssue);
            }
            if(!perfIssues.isEmpty()) {
                perfIssuesByPerfRecord.put(perfRecord, perfIssues);
            }
        }
        return perfIssuesByPerfRecord;
    }

    private void cleanResources() {
        List<RecordablePerformance> perfRecorders = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            perfRecorder.cleanResources();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Annotation, PerfMeasure> extractPerfMeasureByAnnotation(SetOfAnnotationConfigs testAnnotationConfigs, Map<Annotation, PerfRecord> perfRecordByAnnotation) {
        Map<Annotation, PerfMeasure> perfMeasureByAnnotation = new HashMap<>();
        for (Annotation annotation : testExecutionContext.getPerfAnnotations()) {
            ExtractablePerformanceMeasure perfMeasureExtractor = testAnnotationConfigs.retrievePerfMeasureExtractorFor(annotation);
            PerfRecord perfRecord = perfRecordByAnnotation.get(annotation);
            PerfMeasure perfMeasure = perfMeasureExtractor.extractPerfMeasureFrom(perfRecord);
            if(perfMeasure != PerfMeasure.NONE) {
                perfMeasureByAnnotation.put(annotation, perfMeasure);
            }
        }
        return perfMeasureByAnnotation;
    }

    private Map<Class<? extends RecordablePerformance>, RecordablePerformance> buildPerfRecorderInstanceByPerfRecorderClass() {
        List<RecordablePerformance> perfRecorders = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
        Map<Class<? extends RecordablePerformance>, RecordablePerformance>
                perfRecorderInstanceByPerfRecorderClass = new HashMap<>();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            perfRecorderInstanceByPerfRecorderClass.put(perfRecorder.getClass(), perfRecorder);
        }
        return perfRecorderInstanceByPerfRecorderClass;
    }

    private Map<Annotation, PerfIssue> evaluatePerfIssuesByAnnotation(
            Map<Annotation, PerfMeasure> perfMeasuredByAnnotation
            , SetOfAnnotationConfigs testAnnotationConfigs) {
        Map<Annotation, PerfIssue> perfIssueByAnnotation = new HashMap<>();
        for (Annotation annotation : perfMeasuredByAnnotation.keySet()) {
            PerfIssue perfIssue = evaluatePerfIssue(perfMeasuredByAnnotation, testAnnotationConfigs, annotation);
            if(perfIssue != PerfIssue.NONE) {
                perfIssueByAnnotation.put(annotation, perfIssue);
            }
        }
        return perfIssueByAnnotation;
    }

    @SuppressWarnings("unchecked")
    private PerfIssue evaluatePerfIssue(Map<Annotation, PerfMeasure> perfMeasuredByAnnotation
            , SetOfAnnotationConfigs testAnnotationConfigs
            , Annotation annotation) {
        PerfMeasure perfMeasure = perfMeasuredByAnnotation.get(annotation);
        VerifiablePerformanceIssue perfIssueVerifier = testAnnotationConfigs.retrievePerfIssuerVerifierFor(annotation);
        return perfIssueVerifier.verifyPerfIssue(annotation, perfMeasure);
    }

}
