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

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIssueTest {

    @Test public void
    a_junit4_assumption_failure_should_be_an_abort() {

        // GIVEN org.junit.AssumptionViolatedException, a subclass of org.junit.internal.AssumptionViolatedException
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");

        // WHEN
        TestIssue testIssue = TestIssue.buildFrom(assumptionFailure);

        // THEN
        assertThat(testIssue.isAbort()).isTrue();
        assertThat(testIssue.asThrowable()).isSameAs(assumptionFailure);

    }

    @Test public void
    a_failure_should_not_be_an_abort() {

        // WHEN
        TestIssue testIssue = TestIssue.buildFrom(new AssertionError("functional failure"));

        // THEN
        assertThat(testIssue.isAbort()).isFalse();

    }

    @Test public void
    no_issue_should_not_be_an_abort() {

        assertThat(TestIssue.NONE.isAbort()).isFalse();
        assertThat(TestIssue.buildFrom(null).isAbort()).isFalse();

    }

    @Test public void
    a_throwable_should_be_thrown_before_the_end_of_the_test_method_by_default() {

        // WHEN
        TestIssue testIssue = TestIssue.buildFrom(new AssumptionViolatedException("assumption not respected"));

        // THEN
        assertThat(testIssue.isThrownAfterTestMethod()).isFalse();

    }

    @Test public void
    an_abort_thrown_after_the_test_method_should_be_marked_as_such() {

        // GIVEN
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");

        // WHEN
        TestIssue testIssue = TestIssue.buildFromThrowableAfterTestMethod(assumptionFailure);

        // THEN
        assertThat(testIssue.isAbort()).isTrue();
        assertThat(testIssue.isThrownAfterTestMethod()).isTrue();
        assertThat(testIssue.asThrowable()).isSameAs(assumptionFailure);

    }

    @Test public void
    an_abort_should_stay_an_abort_when_replaced_by_a_serializable_test_issue() {

        // GIVEN
        TestIssue testIssue = TestIssue.buildFrom(new AssumptionViolatedException("assumption not respected"));

        // WHEN
        TestIssue serializableTestIssue = TestIssue.buildSerializableTestIssueFrom(testIssue);

        // THEN
        assertThat(serializableTestIssue.asThrowable()).isInstanceOf(TestException.class)
                                                       .hasMessageContaining("assumption not respected");
        assertThat(serializableTestIssue.isAbort()).isTrue();
        assertThat(serializableTestIssue.isThrownAfterTestMethod()).isFalse();

    }

    @Test public void
    an_abort_thrown_after_the_test_method_should_stay_marked_when_replaced_by_a_serializable_test_issue() {

        // GIVEN
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");
        TestIssue testIssue = TestIssue.buildFromThrowableAfterTestMethod(assumptionFailure);

        // WHEN
        TestIssue serializableTestIssue = TestIssue.buildSerializableTestIssueFrom(testIssue);

        // THEN
        assertThat(serializableTestIssue.isAbort()).isTrue();
        assertThat(serializableTestIssue.isThrownAfterTestMethod()).isTrue();

    }

    // As in JUnit 4, which reports each throwable of a MultipleFailureException as a failure: an assumption failing in
    // the test method, and then an @After method failing, for example
    @Test public void
    the_failures_of_a_test_in_a_new_jvm_should_not_be_an_abort_even_if_one_is_an_abort_exception() {

        // GIVEN
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");
        IllegalStateException afterMethodFailure = new IllegalStateException("@After failure");

        // WHEN
        TestIssue testIssue = TestIssue.buildInNewJvmFrom(Arrays.<Throwable>asList(assumptionFailure, afterMethodFailure));

        // THEN
        assertThat(testIssue.isAbort()).isFalse();

    }

    @Test public void
    the_failure_of_a_test_in_a_new_jvm_should_not_be_an_abort_even_if_caused_by_an_abort_exception() {

        // GIVEN
        AssumptionViolatedException assumptionFailure = new AssumptionViolatedException("assumption not respected");
        RuntimeException failure = new RuntimeException(assumptionFailure);

        // WHEN
        TestIssue testIssue = TestIssue.buildInNewJvmFrom(Collections.<Throwable>singletonList(failure));

        // THEN
        assertThat(testIssue.isAbort()).isFalse();

    }

    // As a TestNG test skipped in a new JVM, reported as failed there with its SkipException
    @Test public void
    an_abort_reported_as_the_failure_of_a_test_in_a_new_jvm_should_stay_an_abort() {

        // GIVEN
        IllegalStateException cause = new IllegalStateException("cause of the abort");
        AssumptionViolatedException abort = new AssumptionViolatedException("assumption not respected", cause);

        // WHEN
        TestIssue testIssue = TestIssue.buildInNewJvmFrom(Collections.<Throwable>singletonList(abort));

        // THEN
        assertThat(testIssue.isAbort()).isTrue();
        assertThat(testIssue.isThrownAfterTestMethod()).isFalse();
        assertThat(testIssue.asThrowable()).as("the abort, and not its cause").isSameAs(abort);

    }

    @Test public void
    a_failure_should_not_become_an_abort_when_replaced_by_a_serializable_test_issue() {

        // GIVEN
        TestIssue testIssue = TestIssue.buildFrom(new AssertionError("functional failure"));

        // WHEN
        TestIssue serializableTestIssue = TestIssue.buildSerializableTestIssueFrom(testIssue);

        // THEN
        assertThat(serializableTestIssue.isAbort()).isFalse();

    }

}
