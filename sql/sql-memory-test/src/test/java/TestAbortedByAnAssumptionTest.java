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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runners.model.Statement;
import org.quickperf.SystemProperties;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.Xmx;
import org.quickperf.sql.Book;
import org.quickperf.sql.annotation.ExpectMaxSelect;
import org.quickperf.sql.annotation.ExpectSelect;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// JUnit 4 reports a test aborted by a failed assumption as skipped. QuickPerf
// evaluates the performance-related properties of such a test only if the
// assumption fails once the test method has completed, in an @After method for
// example: the performance recording of the test method is then complete.
public class TestAbortedByAnAssumptionTest {

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInABeforeMethod extends SqlTestBase {

        @Before
        public void assume_that_the_database_is_available() {
            Assume.assumeTrue("Database not available", false);
        }

        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            executeSelects(emf, 1);
        }

    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_test_aborted_in_a_before_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeMethod.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingAtTheBeginningOfTheTestMethod extends SqlTestBase {

        @ExpectSelect(1)
        @Test
        public void execute_one_select_if_the_database_is_available() {
            Assume.assumeTrue("Database not available", false);
            executeSelects(emf, 1);
        }

    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_test_aborted_in_the_test_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingAtTheBeginningOfTheTestMethod.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInAnAfterMethod extends SqlTestBase {

        @ExpectMaxSelect(1)
        @Test
        public void execute_three_selects() {
            executeSelects(emf, 3);
        }

        @After
        public void assume_in_an_after_method() {
            Assume.assumeTrue("Assumption failing in an @After method", false);
        }

    }

    @Test public void
    should_report_the_performance_issues_and_the_assumption_of_a_test_aborted_in_an_after_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterMethod.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.failureMessage()).as(testResult.toString())
                .contains("Performance-related and functional properties not respected")
                .contains("You may think that at most <1> select statement was sent to the database")
                .contains("But there are in fact <3>...")
                .contains("Assumption failing in an @After method");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingAtTheBeginningOfATestMethodWithAMaxOfSelectsInANewJvm extends SqlTestBase {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectMaxSelect(5)
        @Test
        public void execute_one_select_if_the_database_is_available() {
            Assume.assumeTrue("Database not available", false);
            executeSelects(emf, 1);
        }

    }

    @Test public void
    should_report_an_assumption_failing_in_the_test_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingAtTheBeginningOfATestMethodWithAMaxOfSelectsInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingAtTheBeginningOfATestMethodWithAnExpectedSelectInANewJvm extends SqlTestBase {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        public void execute_one_select_if_the_database_is_available() {
            Assume.assumeTrue("Database not available", false);
            executeSelects(emf, 1);
        }

    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_test_aborted_in_the_test_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingAtTheBeginningOfATestMethodWithAnExpectedSelectInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInAnAfterMethodInANewJvm extends SqlTestBase {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectMaxSelect(1)
        @Test
        public void execute_three_selects() {
            executeSelects(emf, 3);
        }

        @After
        public void assume_in_an_after_method() {
            Assume.assumeTrue("Assumption failing in an @After method", false);
        }

    }

    @Test public void
    should_report_the_performance_issues_and_the_assumption_of_a_test_aborted_in_an_after_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterMethodInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.failureMessage()).as(testResult.toString())
                .contains("Performance-related and functional properties not respected")
                .contains("You may think that at most <1> select statement was sent to the database")
                .contains("But there are in fact <3>...")
                .contains("Assumption failing in an @After method");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInABeforeMethodInANewJvm extends SqlTestBase {

        @Before
        public void assume_that_the_database_is_available() {
            Assume.assumeTrue("Database not available", false);
        }

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            executeSelects(emf, 1);
        }

    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_test_aborted_in_a_before_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeMethodInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInAnAfterMethodOfATestExpectingAnException extends SqlTestBase {

        @ExpectMaxSelect(1)
        @Test(expected = IllegalStateException.class)
        public void execute_three_selects_and_throw_the_expected_exception() {
            executeSelects(emf, 3);
            throw new IllegalStateException();
        }

        @After
        public void assume_in_an_after_method() {
            Assume.assumeTrue("Assumption failing in an @After method", false);
        }

    }

    @Test public void
    should_report_the_performance_issues_of_a_test_method_throwing_its_expected_exception_and_aborted_in_an_after_method() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterMethodOfATestExpectingAnException.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.failureMessage()).as(testResult.toString())
                .contains("You may think that at most <1> select statement was sent to the database")
                .contains("Assumption failing in an @After method");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInAnAfterMethodOfATestExpectingAnExceptionInANewJvm extends SqlTestBase {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectMaxSelect(1)
        @Test(expected = IllegalStateException.class)
        public void execute_three_selects_and_throw_the_expected_exception() {
            executeSelects(emf, 3);
            throw new IllegalStateException();
        }

        @After
        public void assume_in_an_after_method() {
            Assume.assumeTrue("Assumption failing in an @After method", false);
        }

    }

    @Test public void
    should_report_the_performance_issues_of_a_test_method_throwing_its_expected_exception_and_aborted_in_an_after_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterMethodOfATestExpectingAnExceptionInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.failureMessage()).as(testResult.toString())
                .contains("You may think that at most <1> select statement was sent to the database")
                .contains("Assumption failing in an @After method");

    }

    // Retries the test once if it fails
    public static class RetryOnFailure implements TestRule {

        @Override
        public Statement apply(final Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        base.evaluate();
                    } catch (AssertionError failure) {
                        base.evaluate();
                    }
                }
            };
        }

    }

    // The first attempt executes the test method and fails in an @After method. The second attempt is aborted in a
    // @Before method: the completion of the test method in the first attempt must not apply to it.
    public abstract static class RetriedTestAbortedInABeforeMethodOfItsSecondAttempt extends SqlTestBase {

        static final AtomicInteger ATTEMPTS = new AtomicInteger();

        @Rule
        public RetryOnFailure retryOnFailure = new RetryOnFailure();

        @Before
        public void assume_that_the_database_is_still_available() {
            int attempt = ATTEMPTS.incrementAndGet();
            Assume.assumeTrue("Database not available any more", attempt == 1);
        }

        @ExpectSelect(1)
        @Test
        public void execute_two_selects() {
            executeSelects(emf, 2);
        }

        @After
        public void fail_the_first_attempt() {
            if (ATTEMPTS.get() == 1) {
                throw new AssertionError("Failure of the first attempt");
            }
        }

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class RetriedTestAbortedInABeforeMethod extends RetriedTestAbortedInABeforeMethodOfItsSecondAttempt {
    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_retried_test_aborted_in_a_before_method() {

        // GIVEN
        RetriedTestAbortedInABeforeMethodOfItsSecondAttempt.ATTEMPTS.set(0);
        Class<?> testClass = RetriedTestAbortedInABeforeMethod.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available any more");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class RetriedTestAbortedInABeforeMethodInANewJvm extends RetriedTestAbortedInABeforeMethodOfItsSecondAttempt {

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        @Override
        public void execute_two_selects() {
            super.execute_two_selects();
        }

    }

    @Test public void
    should_not_evaluate_the_performance_properties_of_a_retried_test_aborted_in_a_before_method_executed_in_a_new_jvm() {

        // GIVEN
        Class<?> testClass = RetriedTestAbortedInABeforeMethodInANewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Database not available any more");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInABeforeClassMethodOfTheNewJvm extends SqlTestBase {

        @BeforeClass
        public static void assume_in_a_before_class_method_of_the_new_jvm() {
            boolean executingInTheNewJvm = SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
            Assume.assumeFalse("Assumption failing in a @BeforeClass method of the new JVM", executingInTheNewJvm);
        }

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            executeSelects(emf, 1);
        }

    }

    // The new JVM, which may not get the system properties of the test JVM, can be the only one where the assumption
    // fails
    @Test public void
    should_not_evaluate_the_performance_properties_of_a_test_aborted_in_a_before_class_method_of_the_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInABeforeClassMethodOfTheNewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isOne();
        assertThat(testResult.assumptionFailureMessage()).as(testResult.toString())
                                                         .contains("Assumption failing in a @BeforeClass method of the new JVM");

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class AssumptionFailingInAnAfterClassMethodOfTheNewJvm extends SqlTestBase {

        @AfterClass
        public static void assume_in_an_after_class_method_of_the_new_jvm() {
            boolean executingInTheNewJvm = SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
            Assume.assumeFalse("Assumption failing in an @AfterClass method of the new JVM", executingInTheNewJvm);
        }

        @Xmx(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @ExpectSelect(1)
        @Test
        public void execute_one_select() {
            executeSelects(emf, 1);
        }

    }

    // The test JVM reports an assumption failing in an @AfterClass method for the class, and not for the test
    @Test public void
    should_not_report_a_test_as_aborted_because_of_an_assumption_failing_in_an_after_class_method_of_the_new_jvm() {

        // GIVEN
        Class<?> testClass = AssumptionFailingInAnAfterClassMethodOfTheNewJvm.class;

        // WHEN
        TestResult testResult = TestResult.of(testClass);

        // THEN
        assertThat(testResult.runCount()).as(testResult.toString()).isOne();
        assertThat(testResult.failureCount()).as(testResult.toString()).isZero();
        assertThat(testResult.assumptionFailureCount()).as(testResult.toString()).isZero();

    }

    private static void executeSelects(EntityManagerFactory emf, int selectCount) {
        EntityManager em = emf.createEntityManager();
        for (int i = 0; i < selectCount; i++) {
            Query query = em.createQuery("FROM " + Book.class.getCanonicalName());
            query.getResultList();
        }
    }

    // The result of JUnit counts the assumption failures, but keeps only the
    // other failures
    private static class TestResult {

        private final Result result;

        private final List<Failure> assumptionFailures;

        private TestResult(Result result, List<Failure> assumptionFailures) {
            this.result = result;
            this.assumptionFailures = assumptionFailures;
        }

        static TestResult of(Class<?> testClass) {
            JUnitCore jUnitCore = new JUnitCore();
            AssumptionFailureRecorder assumptionFailureRecorder = new AssumptionFailureRecorder();
            jUnitCore.addListener(assumptionFailureRecorder);
            Result result = jUnitCore.run(testClass);
            return new TestResult(result, assumptionFailureRecorder.assumptionFailures);
        }

        int runCount() {
            return result.getRunCount();
        }

        int failureCount() {
            return result.getFailureCount();
        }

        int assumptionFailureCount() {
            return result.getAssumptionFailureCount();
        }

        String failureMessage() {
            return result.getFailures().get(0).getMessage();
        }

        String assumptionFailureMessage() {
            return assumptionFailures.get(0).getMessage();
        }

        @Override
        public String toString() {
            StringBuilder report = new StringBuilder();
            report.append(result.getRunCount()).append(" test(s) run");
            appendFailures(report, "failure", result.getFailures());
            appendFailures(report, "assumption failure", assumptionFailures);
            return report.toString();
        }

        private static void appendFailures(StringBuilder report, String failureType, List<Failure> failures) {
            report.append(System.lineSeparator()).append(failures.size()).append(" ").append(failureType).append("(s)");
            for (Failure failure : failures) {
                report.append(System.lineSeparator()).append(failure.getTrace());
            }
        }

    }

    private static class AssumptionFailureRecorder extends RunListener {

        private final List<Failure> assumptionFailures = new CopyOnWriteArrayList<>();

        @Override
        public void testAssumptionFailure(Failure failure) {
            assumptionFailures.add(failure);
        }

    }

}
