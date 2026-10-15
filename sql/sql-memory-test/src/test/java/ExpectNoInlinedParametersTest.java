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
import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.annotation.AllowInlinedParameters;
import org.quickperf.sql.annotation.DisableQueriesWithoutBindParameters;
import org.quickperf.sql.annotation.EnableQueriesWithoutBindParameters;
import org.quickperf.sql.annotation.ExpectNoInlinedParameters;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

@SuppressWarnings("deprecation")
public class ExpectNoInlinedParametersTest {

    private static final String QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE = "[PERF] Query without bind parameters";

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectWithBindParameters extends SqlTestBase {

        @ExpectNoInlinedParameters
        @Test
        public void execute_one_select_with_bind_parameters() {
            executeOneSelectWithBindParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_pass_with_bind_parameters() {

        // GIVEN
        Class<?> testClass = SelectWithBindParameters.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectWithInlinedParameters extends SqlTestBase {

        @ExpectNoInlinedParameters
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_fail_with_inlined_parameters() {

        // GIVEN
        Class<?> testClass = SelectWithInlinedParameters.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectWithBindParametersInADedicatedJVM extends SqlTestBase {

        @ExpectNoInlinedParameters
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_bind_parameters() {
            executeOneSelectWithBindParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_pass_with_bind_parameters_and_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = SelectWithBindParametersInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    public static class SelectWithInlinedParametersInADedicatedJVM extends SqlTestBase {

        @ExpectNoInlinedParameters
        @HeapSize(value = 20, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_fail_with_inlined_parameters_in_a_dedicated_jvm() {

        // GIVEN
        Class<?> testClass = SelectWithInlinedParametersInADedicatedJVM.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).contains(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoInlinedParameters
    public static class InlinedParametersAllowedOnMethod extends SqlTestBase {

        @AllowInlinedParameters(comment = "Skewed data")
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_pass_if_inlined_parameters_are_allowed() {

        // GIVEN
        Class<?> testClass = InlinedParametersAllowedOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableQueriesWithoutBindParameters
    public static class DeprecatedAnnotationOnClassAndInlinedParametersAllowedOnMethod extends SqlTestBase {

        @AllowInlinedParameters
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    allow_inlined_parameters_should_cancel_the_deprecated_disable_queries_without_bind_parameters() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndInlinedParametersAllowedOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @ExpectNoInlinedParameters
    public static class DeprecatedEnableAnnotationOnMethod extends SqlTestBase {

        @EnableQueriesWithoutBindParameters
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    the_deprecated_enable_queries_without_bind_parameters_should_cancel_expect_no_inlined_parameters() {

        // GIVEN
        Class<?> testClass = DeprecatedEnableAnnotationOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isZero();

    }

    @RunWith(QuickPerfJUnitRunner.class)
    @DisableQueriesWithoutBindParameters
    public static class DeprecatedAnnotationOnClassAndNewAnnotationOnMethod extends SqlTestBase {

        @ExpectNoInlinedParameters
        @Test
        public void execute_one_select_with_inlined_parameters() {
            executeOneSelectWithInlinedParameters(emf.createEntityManager());
        }

    }

    @Test public void
    should_report_inlined_parameters_once_if_the_deprecated_and_the_new_annotations_are_both_applied() {

        // GIVEN
        Class<?> testClass = DeprecatedAnnotationOnClassAndNewAnnotationOnMethod.class;

        // WHEN
        PrintableResult testResult = testResult(testClass);

        // THEN
        assertThat(testResult.failureCount()).isOne();

        assertThat(testResult.toString()).containsOnlyOnce(QUERY_WITHOUT_BIND_PARAMETERS_MESSAGE);

    }

    private static void executeOneSelectWithBindParameters(EntityManager em) {
        String sql = "SELECT * FROM book WHERE isbn = :isbn AND title = :title";
        Query nativeQuery = em.createNativeQuery(sql)
                .setParameter("isbn", "978-0321356680")
                .setParameter("title", "Effective Java");
        nativeQuery.getResultList();
    }

    private static void executeOneSelectWithInlinedParameters(EntityManager em) {
        String sql = "SELECT * FROM book WHERE isbn = '978-0321356680'";
        Query nativeQuery = em.createNativeQuery(sql);
        nativeQuery.getResultList();
    }

}
