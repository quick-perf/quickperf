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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf;

import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.perfrecording.PerfRecord;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.perfrecording.ViewablePerfRecordIfPerfIssue;
import org.quickperf.repository.BusinessOrTechnicalIssueRepository;

import java.lang.annotation.Annotation;
import java.util.*;

public class PerfIssuesEvaluator {

    private final BusinessOrTechnicalIssueRepository businessOrTechnicalIssueRepository = BusinessOrTechnicalIssueRepository.INSTANCE;

    private PerfIssuesEvaluator() {}

    public static final PerfIssuesEvaluator INSTANCE = new PerfIssuesEvaluator();

    public Collection<PerfIssuesToFormat> evaluatePerfIssues(SetOfAnnotationConfigs testAnnotationConfigs, TestExecutionContext testExecutionContext) {
        if(testExecutionContext.isQuickPerfDisabled()) {
            return Collections.emptyList();
        }
        Map<Annotation, PerfRecord> perfRecordByAnnotation
                = buildPerfRecordByAnnotation(testAnnotationConfigs, testExecutionContext);

        Map<Annotation, PerfIssue> perfIssuesByAnnotation
                = evaluatePerfIssuesByAnnotation(perfRecordByAnnotation, testExecutionContext, testAnnotationConfigs);

        return perfIssuesToFormatGroup(perfRecordByAnnotation, perfIssuesByAnnotation);
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
            BusinessOrTechnicalIssue businessOrTechnicalIssueFromTestJvm = businessOrTechnicalIssueRepository.findFrom(workingFolder);
            if (!businessOrTechnicalIssueFromTestJvm.isNone()) {
                Throwable throwable = businessOrTechnicalIssueFromTestJvm.getThrowable();
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

    @SuppressWarnings("unchecked")
    public Map<Annotation, PerfMeasure> extractPerfMeasureByAnnotation(SetOfAnnotationConfigs testAnnotationConfigs, Map<Annotation, PerfRecord> perfRecordByAnnotation, TestExecutionContext testExecutionContext) {
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
