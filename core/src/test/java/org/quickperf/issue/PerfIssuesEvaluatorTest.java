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

import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.AnnotationConfig;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.measure.BooleanMeasure;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.perfrecording.PerfRecord;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.perfrecording.SelfVerifiablePerfRecord;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PerfIssuesEvaluatorTest {

    private static final String RECORD_ISSUE = "The recording itself cannot be trusted.";

    private static final String ANNOTATION_ISSUE = "An annotation-level property is not respected.";

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotationWithoutIssue {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface OtherPerfAnnotationWithoutIssue {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotationWithIssue {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotationOfOtherRecorder {}

    @Retention(RetentionPolicy.RUNTIME)
    private @interface PerfAnnotationOfFormattingRecorder {}

    @PerfAnnotationWithoutIssue
    @OtherPerfAnnotationWithoutIssue
    @PerfAnnotationWithIssue
    @PerfAnnotationOfOtherRecorder
    @PerfAnnotationOfFormattingRecorder
    private static class AnnotatedClass {}

    @Test public void
    a_self_verifiable_record_issue_should_be_reported_once_even_if_annotations_are_respected() {

        // GIVEN a record reporting an issue of its own, shared by two annotations without issue
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithoutIssue.class, OtherPerfAnnotationWithoutIssue.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        PerfIssuesToFormat recordPerfIssues = perfIssuesToFormat.iterator().next();
        assertThat(recordPerfIssues.getNumberOfPerfIssues()).isOne();
        assertThat(recordPerfIssues.format()).isEqualTo("[PERF] " + RECORD_ISSUE);

    }

    @Test public void
    a_self_verifiable_record_issue_should_be_reported_before_annotation_issues() {

        // GIVEN a record reporting an issue of its own, and an annotation not respected
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithIssue.class, PerfAnnotationWithoutIssue.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        PerfIssuesToFormat recordPerfIssues = perfIssuesToFormat.iterator().next();
        assertThat(recordPerfIssues.getNumberOfPerfIssues()).isEqualTo(2);
        String formattedIssues = recordPerfIssues.format();
        assertThat(formattedIssues.indexOf(RECORD_ISSUE)).isNotNegative()
                                                         .isLessThan(formattedIssues.indexOf(ANNOTATION_ISSUE));

    }

    @Test public void
    an_issue_reported_by_several_records_should_be_reported_once() {

        // GIVEN two records of the test, from two recorders, reporting the same issue
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        OtherFakeRecorder otherFakeRecorder = new OtherFakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(Arrays.<RecordablePerformance>asList(fakeRecorder, otherFakeRecorder)
                                   , PerfAnnotationWithoutIssue.class, PerfAnnotationOfOtherRecorder.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        PerfIssuesToFormat recordPerfIssues = perfIssuesToFormat.iterator().next();
        assertThat(recordPerfIssues.getNumberOfPerfIssues()).isOne();
        assertThat(recordPerfIssues.format()).isEqualTo("[PERF] " + RECORD_ISSUE);

    }

    @Test public void
    an_issue_reported_by_several_records_should_be_reported_with_the_record_formatting_its_issues() {

        // GIVEN two records reporting the same issue, one formatting its perf issues itself
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        FormattingFakeRecorder formattingFakeRecorder = new FormattingFakeRecorder(new FormattingFakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(Arrays.<RecordablePerformance>asList(fakeRecorder, formattingFakeRecorder)
                                   , PerfAnnotationWithoutIssue.class, PerfAnnotationOfFormattingRecorder.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        assertThat(perfIssuesToFormat.iterator().next().format())
                .isEqualTo(FormattingFakeRecord.FORMAT_PREFIX + RECORD_ISSUE);

    }

    @Test public void
    the_issue_of_a_record_should_be_reported_before_the_issues_of_other_records() {

        // GIVEN a record with an annotation issue and another record reporting an issue of its own
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(PerfIssue.NONE));
        OtherFakeRecorder otherFakeRecorder = new OtherFakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(Arrays.<RecordablePerformance>asList(fakeRecorder, otherFakeRecorder)
                                   , PerfAnnotationWithIssue.class, PerfAnnotationOfOtherRecorder.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(2);
        assertThat(perfIssuesToFormat.iterator().next().format()).isEqualTo("[PERF] " + RECORD_ISSUE);

    }

    @Test public void
    a_trustworthy_self_verifiable_record_should_not_report_any_issue() {

        // GIVEN
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(PerfIssue.NONE));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithoutIssue.class, OtherPerfAnnotationWithoutIssue.class);

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext);

        // THEN
        assertThat(perfIssuesToFormat).isEmpty();

    }

    @Test public void
    the_performance_of_a_test_aborted_before_the_end_of_its_test_method_should_not_be_evaluated() {

        // GIVEN a record reporting an issue of its own, an annotation not respected, and an abort
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithIssue.class);
        TestIssue abort = TestIssue.buildFrom(new AssumptionViolatedException("assumption not respected"));

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext, abort);

        // THEN
        assertThat(perfIssuesToFormat).isEmpty();

    }

    @Test public void
    the_records_of_a_test_aborted_before_the_end_of_its_test_method_should_not_be_read() {

        // GIVEN a recorder without record, as in the test JVM when a @Before method aborted the test in the specific JVM
        FakeRecorder recorderWithoutRecord = new RecorderWithoutRecord();
        TestExecutionContext testExecutionContext =
                testExecutionContext(recorderWithoutRecord, PerfAnnotationWithIssue.class);
        TestIssue abort = TestIssue.buildFrom(new AssumptionViolatedException("assumption not respected"));

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext, abort);

        // THEN
        assertThat(perfIssuesToFormat).isEmpty();

    }

    @Test public void
    the_performance_of_a_test_aborted_after_its_test_method_should_be_evaluated() {

        // GIVEN
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(new PerfIssue(RECORD_ISSUE)));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithIssue.class);
        TestIssue abortAfterTestMethod =
                TestIssue.buildFromThrowableAfterTestMethod(new AssumptionViolatedException("assumption not respected"));

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext, abortAfterTestMethod);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        String formattedIssues = perfIssuesToFormat.iterator().next().format();
        assertThat(formattedIssues).contains(RECORD_ISSUE)
                                   .contains(ANNOTATION_ISSUE);

    }

    @Test public void
    the_performance_of_a_failing_test_should_be_evaluated() {

        // GIVEN
        FakeRecorder fakeRecorder = new FakeRecorder(new FakeRecord(PerfIssue.NONE));
        TestExecutionContext testExecutionContext =
                testExecutionContext(fakeRecorder, PerfAnnotationWithIssue.class);
        TestIssue failure = TestIssue.buildFrom(new AssertionError("functional failure"));

        // WHEN
        Collection<PerfIssuesToFormat> perfIssuesToFormat = evaluate(testExecutionContext, failure);

        // THEN
        assertThat(perfIssuesToFormat).hasSize(1);
        assertThat(perfIssuesToFormat.iterator().next().format()).contains(ANNOTATION_ISSUE);

    }

    private Collection<PerfIssuesToFormat> evaluate(TestExecutionContext testExecutionContext) {
        return evaluate(testExecutionContext, TestIssue.NONE);
    }

    private Collection<PerfIssuesToFormat> evaluate(TestExecutionContext testExecutionContext, TestIssue testIssue) {
        return PerfIssuesEvaluator.INSTANCE.evaluatePerfIssuesIfNoJvmIssue(annotationConfigs()
                                                                         , testExecutionContext
                                                                         , JvmOrTestIssue.buildFrom(testIssue));
    }

    private SetOfAnnotationConfigs annotationConfigs() {
        AnnotationConfig withoutIssue = new AnnotationConfig.Builder()
                .perfRecorderClass(FakeRecorder.class)
                .build(PerfAnnotationWithoutIssue.class);
        AnnotationConfig otherWithoutIssue = new AnnotationConfig.Builder()
                .perfRecorderClass(FakeRecorder.class)
                .build(OtherPerfAnnotationWithoutIssue.class);
        AnnotationConfig withIssue = new AnnotationConfig.Builder()
                .perfRecorderClass(FakeRecorder.class)
                .perfMeasureExtractor(ALWAYS_TRUE_MEASURE)
                .perfIssueVerifier(ALWAYS_ANNOTATION_ISSUE)
                .build(PerfAnnotationWithIssue.class);
        AnnotationConfig ofOtherRecorder = new AnnotationConfig.Builder()
                .perfRecorderClass(OtherFakeRecorder.class)
                .build(PerfAnnotationOfOtherRecorder.class);
        AnnotationConfig ofFormattingRecorder = new AnnotationConfig.Builder()
                .perfRecorderClass(FormattingFakeRecorder.class)
                .build(PerfAnnotationOfFormattingRecorder.class);
        return new SetOfAnnotationConfigs(Arrays.asList(withoutIssue, otherWithoutIssue, withIssue, ofOtherRecorder, ofFormattingRecorder));
    }

    @SafeVarargs
    private final TestExecutionContext testExecutionContext(FakeRecorder fakeRecorder, Class<? extends Annotation>... annotationClasses) {
        return testExecutionContext(Collections.<RecordablePerformance>singletonList(fakeRecorder), annotationClasses);
    }

    @SafeVarargs
    private final TestExecutionContext testExecutionContext(List<RecordablePerformance> perfRecorders, Class<? extends Annotation>... annotationClasses) {
        List<Annotation> perfAnnotations = new ArrayList<>();
        for (Class<? extends Annotation> annotationClass : annotationClasses) {
            perfAnnotations.add(AnnotatedClass.class.getAnnotation(annotationClass));
        }
        TestExecutionContext testExecutionContext = mock(TestExecutionContext.class);
        when(testExecutionContext.getPerfAnnotations()).thenReturn(perfAnnotations.toArray(new Annotation[0]));
        when(testExecutionContext.getPerfRecordersToExecuteAfterTestMethod()).thenReturn(perfRecorders);
        return testExecutionContext;
    }

    private static final ExtractablePerformanceMeasure ALWAYS_TRUE_MEASURE = new ExtractablePerformanceMeasure() {
        @Override
        public PerfMeasure extractPerfMeasureFrom(PerfRecord perfRecord) {
            return BooleanMeasure.TRUE;
        }
    };

    private static final VerifiablePerformanceIssue ALWAYS_ANNOTATION_ISSUE = new VerifiablePerformanceIssue() {
        @Override
        public PerfIssue verifyPerfIssue(Annotation annotation, PerfMeasure measure) {
            return new PerfIssue(ANNOTATION_ISSUE);
        }
    };

    private static class FakeRecord implements SelfVerifiablePerfRecord {

        private final PerfIssue recordIssue;

        FakeRecord(PerfIssue recordIssue) {
            this.recordIssue = recordIssue;
        }

        @Override
        public PerfIssue verifyRecord() {
            return recordIssue;
        }

    }

    private static class FakeRecorder implements RecordablePerformance<FakeRecord> {

        private final FakeRecord fakeRecord;

        FakeRecorder(FakeRecord fakeRecord) {
            this.fakeRecord = fakeRecord;
        }

        @Override
        public void startRecording(TestExecutionContext testExecutionContext) { }

        @Override
        public void stopRecording(TestExecutionContext testExecutionContext) { }

        @Override
        public FakeRecord findRecord(TestExecutionContext testExecutionContext) {
            return fakeRecord;
        }

        @Override
        public void cleanResources() { }

    }

    private static class RecorderWithoutRecord extends FakeRecorder {

        RecorderWithoutRecord() {
            super(null);
        }

        @Override
        public FakeRecord findRecord(TestExecutionContext testExecutionContext) {
            throw new IllegalStateException("No record to read");
        }

    }

    private static class OtherFakeRecorder extends FakeRecorder {

        OtherFakeRecorder(FakeRecord fakeRecord) {
            super(fakeRecord);
        }

    }

    private static class FormattingFakeRecord extends FakeRecord implements PerfIssuesFormat {

        static final String FORMAT_PREFIX = "Formatted by the record: ";

        FormattingFakeRecord(PerfIssue recordIssue) {
            super(recordIssue);
        }

        @Override
        public String format(Collection<PerfIssue> perfIssues) {
            return FORMAT_PREFIX + PerfIssuesFormat.STANDARD.format(perfIssues).replace("[PERF] ", "");
        }

    }

    private static class FormattingFakeRecorder extends FakeRecorder {

        FormattingFakeRecorder(FormattingFakeRecord formattingFakeRecord) {
            super(formattingFakeRecord);
        }

    }

}
