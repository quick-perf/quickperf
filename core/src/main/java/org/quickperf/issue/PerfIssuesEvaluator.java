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
package org.quickperf.issue;

import org.quickperf.*;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.perfrecording.PerfRecord;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.perfrecording.SelfVerifiablePerfRecord;
import org.quickperf.perfrecording.ViewablePerfRecordIfPerfIssue;
import org.quickperf.repository.TestIssueRepository;

import java.lang.annotation.Annotation;
import java.util.*;

public class PerfIssuesEvaluator {

    private final TestIssueRepository testIssueRepository = TestIssueRepository.INSTANCE;

    private PerfIssuesEvaluator() {}

    public static final PerfIssuesEvaluator INSTANCE = new PerfIssuesEvaluator();

    public Collection<PerfIssuesToFormat> evaluatePerfIssuesIfNoJvmIssue(SetOfAnnotationConfigs annotationConfigs
                                                                       , TestExecutionContext testExecutionContext
                                                                       , JvmOrTestIssue jvmOrTestIssue) {

        try {
            return evaluatePerfIssuesIfNoJvmIssueWithPotentialUnexpectedException(annotationConfigs
                                                                                , testExecutionContext
                                                                                , jvmOrTestIssue);
        } catch (Exception e) {
            if(UnexpectedQuickPerfException.isQuickPerfException(e)) {
                throw e;
            }
            throw new UnexpectedQuickPerfException(e);
        }

    }

    private Collection<PerfIssuesToFormat> evaluatePerfIssuesIfNoJvmIssueWithPotentialUnexpectedException(SetOfAnnotationConfigs annotationConfigs
                                                                                                        , TestExecutionContext testExecutionContext
                                                                                                        , JvmOrTestIssue jvmOrTestIssue) {

        if(jvmOrTestIssue.hasJvmIssue()) {
            return Collections.emptyList();
        }

        if(isAbortBeforeTheEndOfTheTestMethod(jvmOrTestIssue.getTestIssue())) {
            return Collections.emptyList();
        }

        Map<Annotation, PerfRecord> perfRecordByAnnotation
                = buildPerfRecordByAnnotation(annotationConfigs, testExecutionContext);

        Map<Annotation, PerfIssue> perfIssuesByAnnotation
                = evaluatePerfIssuesByAnnotation(perfRecordByAnnotation, testExecutionContext, annotationConfigs);

        return perfIssuesToFormatGroup(perfRecordByAnnotation, perfIssuesByAnnotation);

    }

    // A test aborted before or by its test method, by a failed assumption for example, stays aborted: the abort cut the
    // performance recording short, or the test method was not executed. The performance-related properties are about
    // the whole test method, so they are not evaluated, as the test frameworks skip the assertions following a failed
    // assumption. This also applies to an abort that the test framework adapter does not mark as thrown after the test
    // method. An abort thrown once the test method has returned, by an @AfterEach method for example, leaves the
    // recording complete: the properties are then evaluated as for a test without abort.
    private static boolean isAbortBeforeTheEndOfTheTestMethod(TestIssue testIssue) {
        return testIssue.isAbort() && !testIssue.isThrownAfterTestMethod();
    }

    private Map<Annotation, PerfIssue> evaluatePerfIssuesByAnnotation(Map<Annotation, PerfRecord> perfRecordByAnnotation, TestExecutionContext testExecutionContext, SetOfAnnotationConfigs testAnnotationConfigs) {
        Map<Annotation, PerfMeasure> perfMeasureByAnnotation
                = extractPerfMeasureByAnnotation(testAnnotationConfigs, perfRecordByAnnotation, testExecutionContext);
        return evaluatePerfIssuesByAnnotation(perfMeasureByAnnotation, testAnnotationConfigs);
    }

    private Map<Annotation, PerfRecord> buildPerfRecordByAnnotation(SetOfAnnotationConfigs testAnnotationConfigs, TestExecutionContext testExecutionContext) {
        Map<Annotation, PerfRecord> perfRecordByAnnotation = new HashMap<>();
        Map<Class<? extends RecordablePerformance>, RecordablePerformance> perfRecorderByPerfRecorderClass = buildPerfRecorderInstanceByPerfRecorderClass(testExecutionContext);
        for (Annotation annotation : testExecutionContext.getPerfAnnotations()) {
            Class<? extends RecordablePerformance> perfRecorderClass = testAnnotationConfigs.retrievePerfRecorderClassFor(annotation);
            RecordablePerformance perfRecorder = perfRecorderByPerfRecorderClass.get(perfRecorderClass);
            if (perfRecorder != null) {
                PerfRecord perfRecord = findPerfRecord(perfRecorder, testExecutionContext);
                perfRecordByAnnotation.put(annotation, perfRecord);

            }
        }
        return perfRecordByAnnotation;
    }

    private PerfRecord findPerfRecord(RecordablePerformance perfRecorder, TestExecutionContext testExecutionContext) {
        try {
            return perfRecorder.findRecord(testExecutionContext);
        } catch (Exception e) {
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            TestIssue testIssue = testIssueRepository.findFrom(workingFolder);
            if (!testIssue.isNone()) {
                Throwable throwable = testIssue.asThrowable();
                e.addSuppressed(throwable);
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
        Map<PerfRecord, List<PerfIssue>> perfIssuesByPerfRecord = new LinkedHashMap<>();
        addIssuesOfRecordsThemselves(perfRecordByAnnotation.values(), perfIssuesByPerfRecord);
        for (Annotation annotation : perfRecordByAnnotation.keySet()) {
            PerfIssue perfIssue = perfIssuesByAnnotation.get(annotation);
            if(perfIssue != null) {
                PerfRecord perfRecord = perfRecordByAnnotation.get(annotation);
                List<PerfIssue> perfIssues = perfIssuesByPerfRecord.get(perfRecord);
                if(perfIssues == null) {
                    perfIssues = new ArrayList<>();
                    perfIssuesByPerfRecord.put(perfRecord, perfIssues);
                }
                perfIssues.add(perfIssue);
            }
        }
        return perfIssuesByPerfRecord;
    }

    // Adds the issues that the records report about their own content,
    // independently of the annotations. For example, the recorded SQL can
    // include SQL executed by another test running in parallel: the
    // annotations would then be verified against wrong measures, so the
    // test fails with this issue even if the annotations are respected.
    // These issues are added before the annotation issues, so they're
    // displayed first.
    // Several records of a test can report the same issue (e.g. both the SQL
    // executions and the JDBC batch sizes containing SQL of another test).
    // It is then added only once, preferably to a record formatting its
    // issues itself, so that the failure message also displays the recorded
    // SQL.
    private void addIssuesOfRecordsThemselves(Collection<PerfRecord> perfRecords, Map<PerfRecord, List<PerfIssue>> perfIssuesByPerfRecord) {
        Set<String> reportedIssueDescriptions = new HashSet<>();
        for (PerfRecord perfRecord : distinctRecordsFormattingTheirIssuesFirst(perfRecords)) {
            PerfIssue recordIssue = verifyRecordItself(perfRecord);
            boolean isNotYetReported =    recordIssue != PerfIssue.NONE
                                       && reportedIssueDescriptions.add(recordIssue.getDescription());
            if (isNotYetReported) {
                List<PerfIssue> perfIssues = new ArrayList<>();
                perfIssues.add(recordIssue);
                perfIssuesByPerfRecord.put(perfRecord, perfIssues);
            }
        }
    }

    private List<PerfRecord> distinctRecordsFormattingTheirIssuesFirst(Collection<PerfRecord> perfRecords) {
        List<PerfRecord> recordsFormattingTheirIssues = new ArrayList<>();
        List<PerfRecord> otherRecords = new ArrayList<>();
        for (PerfRecord perfRecord : new LinkedHashSet<>(perfRecords)) {
            if (perfRecord instanceof PerfIssuesFormat) {
                recordsFormattingTheirIssues.add(perfRecord);
            } else {
                otherRecords.add(perfRecord);
            }
        }
        recordsFormattingTheirIssues.addAll(otherRecords);
        return recordsFormattingTheirIssues;
    }

    private PerfIssue verifyRecordItself(PerfRecord perfRecord) {
        if (!(perfRecord instanceof SelfVerifiablePerfRecord)) {
            return PerfIssue.NONE;
        }
        PerfIssue recordIssue = ((SelfVerifiablePerfRecord) perfRecord).verifyRecord();
        return recordIssue == null ? PerfIssue.NONE : recordIssue;
    }

    @SuppressWarnings("unchecked")
    private Map<Annotation, PerfMeasure> extractPerfMeasureByAnnotation(SetOfAnnotationConfigs testAnnotationConfigs, Map<Annotation, PerfRecord> perfRecordByAnnotation, TestExecutionContext testExecutionContext) {
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

    private Map<Class<? extends RecordablePerformance>, RecordablePerformance> buildPerfRecorderInstanceByPerfRecorderClass(TestExecutionContext testExecutionContext) {
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
