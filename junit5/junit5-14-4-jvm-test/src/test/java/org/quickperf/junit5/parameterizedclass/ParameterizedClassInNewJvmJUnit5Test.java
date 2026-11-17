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
package org.quickperf.junit5.parameterizedclass;

import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quickperf.SystemProperties;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectMaxHeapAllocation;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.junit5.JvmExecutionReport.assertThatEachTestWasExecutedInItsOwnNewJvm;
import static org.quickperf.junit5.JvmExecutionReport.reportExecutionOf;

/**
 * QuickPerf executes each test of each invocation of a <code>@ParameterizedClass</code> having an annotation that
 * needs a specific JVM (<code>@HeapSize</code>, <code>@ExpectMaxHeapAllocation</code>...) in its own JVM. This JVM
 * has to execute this test once, with the arguments of its class invocation, and QuickPerf has to report the measures
 * and the failures of this execution for this test.
 * <p>
 * Some test methods of the nested test classes fail on purpose with a {@link JvmExecutionReport}, to tell where they
 * were executed. Others fail if their JVM executes them more than once. QuickPerf fails a test that JUnit does not
 * execute in the specific JVM. In the test JVM, the argument of the invocation #n of each parameterized class having
 * int arguments is n.
 */
public class ParameterizedClassInNewJvmJUnit5Test {

    private static final Pattern CLASS_INVOCATION_INDEX = Pattern.compile("\\[class-template-invocation:#(\\d+)]");

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2, 3})
    static class ParameterizedClassFailingIfExecutedAgainInTheSameJvm {

        private static final AtomicInteger EXECUTIONS_IN_THIS_JVM = new AtomicInteger();

        private final int value;

        public ParameterizedClassFailingIfExecutedAgainInTheSameJvm(int value) {
            this.value = value;
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            assertThat(EXECUTIONS_IN_THIS_JVM.incrementAndGet()).as("Executions in this JVM, value %s", value).isOne();
        }

    }

    @Test public void
    should_execute_the_test_of_each_invocation_of_a_parameterized_class_once_in_its_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassFailingIfExecutedAgainInTheSameJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as(jUnit5TestsResult.getErrorReport())
                .isZero();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(3);

    }

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2, 3})
    static class ParameterizedClassInASpecificJvm {

        private final int value;

        public ParameterizedClassInASpecificJvm(int value) {
            this.value = value;
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            throw reportExecutionOf("test_method() with value " + value);
        }

    }

    @Test public void
    should_execute_the_test_of_each_invocation_of_a_parameterized_class_in_its_own_jvm_with_its_argument() {

        // GIVEN
        Class<?> testClass = ParameterizedClassInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test of each class invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(3);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("test_method() with value 1"
                                                                , "test_method() with value 2"
                                                                , "test_method() with value 3");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestExecutedItsOwnInvocation(jvmExecutionReports);

    }

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2})
    static class ParameterizedClassHavingAnInjectedFieldAndTwoTestMethodsInASpecificJvm {

        @Parameter
        int value;

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void first_test_method() {
            throw reportExecutionOf("first_test_method() with value " + value);
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void second_test_method() {
            throw reportExecutionOf("second_test_method() with value " + value);
        }

    }

    @Test public void
    should_execute_each_test_of_each_invocation_of_a_parameterized_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassHavingAnInjectedFieldAndTwoTestMethodsInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each test of each class invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(4);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("first_test_method() with value 1"
                                                                , "first_test_method() with value 2"
                                                                , "second_test_method() with value 1"
                                                                , "second_test_method() with value 2");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestExecutedItsOwnInvocation(jvmExecutionReports);

    }

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2})
    static class ParameterizedClassHavingARepeatedTestInASpecificJvm {

        private final int value;

        public ParameterizedClassHavingARepeatedTestInASpecificJvm(int value) {
            this.value = value;
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @RepeatedTest(2)
        public void repeated_test(RepetitionInfo repetitionInfo) {
            throw reportExecutionOf("repetition " + repetitionInfo.getCurrentRepetition()
                                  + " of " + repetitionInfo.getTotalRepetitions() + " with value " + value);
        }

    }

    @Test public void
    should_execute_each_repetition_of_each_invocation_of_a_parameterized_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassHavingARepeatedTestInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Each repetition of each class invocation should fail with the report of its execution.%s"
                  , errorReport)
                .isEqualTo(4);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("repetition 1 of 2 with value 1"
                                                                , "repetition 2 of 2 with value 1"
                                                                , "repetition 1 of 2 with value 2"
                                                                , "repetition 2 of 2 with value 2");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestExecutedItsOwnInvocation(jvmExecutionReports);

    }

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(ints = {1, 2})
    static class ParameterizedClassHavingANestedTestClassInASpecificJvm {

        private final int value;

        public ParameterizedClassHavingANestedTestClassInASpecificJvm(int value) {
            this.value = value;
        }

        @Nested
        class NestedTestClass {

            @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
            @Test
            public void test_method() {
                throw reportExecutionOf("test_method() with value " + value);
            }

        }

    }

    @Test public void
    should_execute_the_test_of_a_nested_class_of_each_invocation_of_a_parameterized_class_in_its_own_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassHavingANestedTestClassInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test of each class invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).extracting("executedTest")
                                       .containsExactlyInAnyOrder("test_method() with value 1"
                                                                , "test_method() with value 2");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThatEachTestExecutedItsOwnInvocation(jvmExecutionReports);

    }

    @QuickPerfTest
    @ParameterizedClass(name = "allocating {0} MB")
    @ValueSource(ints = {1, 2, 3})
    static class ParameterizedClassAllocatingAccordingToItsArgument {

        private final int allocationInMegaBytes;

        public ParameterizedClassAllocatingAccordingToItsArgument(int allocationInMegaBytes) {
            this.allocationInMegaBytes = allocationInMegaBytes;
        }

        @ExpectMaxHeapAllocation(value = 2.5, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            byte[] allocatedBytes = new byte[allocationInMegaBytes * 1024 * 1024];
        }

    }

    @Test public void
    should_report_the_heap_allocation_of_the_test_of_each_invocation_of_a_parameterized_class_for_this_test() {

        // GIVEN
        Class<?> testClass = ParameterizedClassAllocatingAccordingToItsArgument.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("Only the test of the class invocation allocating 3 MB should fail.%s", errorReport)
                .isOne();

        assertThat(jUnit5TestsResult.getNumberOfSucceededTests()).isEqualTo(2);

        assertThat(errorReport).contains("allocating 3 MB")
                               .contains("Expected heap allocation (test method thread) to be less than 2.50 Mega bytes but is 3.")
                               .doesNotContain("allocating 1 MB")
                               .doesNotContain("allocating 2 MB");

    }

    @QuickPerfTest
    @ParameterizedClass
    @MethodSource("values")
    static class ParameterizedClassWithReorderedArgumentsInTheSpecificJvm {

        static IntStream values() {
            boolean executingInTheSpecificJvm = SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate();
            return executingInTheSpecificJvm ? IntStream.of(2, 1) : IntStream.of(1, 2);
        }

        private final int value;

        public ParameterizedClassWithReorderedArgumentsInTheSpecificJvm(int value) {
            this.value = value;
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            throw reportExecutionOf("test_method() with value " + value);
        }

    }

    @Test public void
    should_fail_the_test_of_a_class_invocation_executed_with_other_arguments_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassWithReorderedArgumentsInTheSpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test of each class invocation should fail, because the specific JVM executes it with other"
                  + " arguments.%s", errorReport)
                .isEqualTo(2);

        assertThat(jUnit5TestsResult.getJvmExecutionReports())
                .as("No test should be executed with the arguments of another class invocation.%s", errorReport)
                .isEmpty();

        assertThat(errorReport).contains("[class-template-invocation:#1]/[method:test_method()], the display name of the"
                                       + " class invocation is \"[1] 2\", and not \"[1] 1\": QuickPerf cannot report the"
                                       + " measures and the failures of this JVM for \"test_method()\".")
                               .contains("[class-template-invocation:#2]/[method:test_method()], the display name of the"
                                       + " class invocation is \"[2] 1\", and not \"[2] 2\"")
                               .contains("@ParameterizedClass(name = \"...\")")
                               .doesNotContain("UnexpectedQuickPerfException");

    }

    @QuickPerfTest
    @ParameterizedClass
    @ValueSource(strings = {"apple", "banana"})
    static class ParameterizedClassHavingADefaultDisplayNameInASpecificJvm {

        // The display name of the class invocation, in the JVM executing the test
        private static String classInvocationDisplayName;

        @RegisterExtension
        static BeforeEachCallback classInvocationDisplayNameRecorder =
                context -> classInvocationDisplayName = context.getParent().get().getDisplayName();

        private final String fruit;

        public ParameterizedClassHavingADefaultDisplayNameInASpecificJvm(String fruit) {
            this.fruit = fruit;
        }

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            throw reportExecutionOf("test_method() with " + fruit + " in " + classInvocationDisplayName);
        }

    }

    @Test public void
    should_execute_the_test_of_each_class_invocation_with_the_display_name_configuration_of_the_test_jvm() {

        // GIVEN
        Class<?> testClass = ParameterizedClassHavingADefaultDisplayNameInASpecificJvm.class;
        Map<String, String> configurationParameters = new HashMap<>();
        configurationParameters.put("junit.jupiter.params.displayname.default", "invocation {index} with {arguments}");
        configurationParameters.put("junit.jupiter.params.displayname.argument.maxlength", "3");
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass, configurationParameters);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test of each class invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        assertThat(errorReport).contains("invocation 1 with ap…")
                               .contains("invocation 2 with ba…");

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(errorReport)
                                       .extracting("executedTest")
                                       .containsExactlyInAnyOrder("test_method() with apple in invocation 1 with ap…"
                                                                , "test_method() with banana in invocation 2 with ba…");

    }

    // Its display names number the class invocations that it created in the JVM. The specific JVM creates only the
    // class invocation of the test that it executes: there, each class invocation is the first one.
    static class CountingClassTemplate implements ClassTemplateInvocationContextProvider {

        private static final AtomicInteger CREATED_INVOCATIONS = new AtomicInteger();

        @Override
        public boolean supportsClassTemplate(ExtensionContext extensionContext) {
            return true;
        }

        @Override
        public Stream<ClassTemplateInvocationContext> provideClassTemplateInvocationContexts(ExtensionContext extensionContext) {
            return Stream.of("apple", "banana").map(fruit -> new ClassTemplateInvocationContext() {
                @Override
                public String getDisplayName(int invocationIndex) {
                    return "class invocation " + CREATED_INVOCATIONS.incrementAndGet() + " with " + fruit;
                }
            });
        }

    }

    @QuickPerfTest
    @ClassTemplate
    @ExtendWith(CountingClassTemplate.class)
    static class ClassTemplateNamedByACountingProviderInASpecificJvm {

        // The display name of the class invocation, in the JVM executing the test
        private static String classInvocationDisplayName;

        @RegisterExtension
        static BeforeEachCallback classInvocationDisplayNameRecorder =
                context -> classInvocationDisplayName = context.getParent().get().getDisplayName();

        @HeapSize(value = 50, unit = AllocationUnit.MEGA_BYTE)
        @Test
        public void test_method() {
            throw reportExecutionOf("test_method() in " + classInvocationDisplayName);
        }

    }

    @Test public void
    should_execute_the_test_of_each_class_invocation_of_a_custom_provider_having_another_display_name_in_the_specific_jvm() {

        // GIVEN
        Class<?> testClass = ClassTemplateNamedByACountingProviderInASpecificJvm.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        String errorReport = jUnit5TestsResult.getErrorReport();

        assertThat(jUnit5TestsResult.getNumberOfFailures())
                .as("The test of each class invocation should fail with the report of its execution.%s", errorReport)
                .isEqualTo(2);

        List<JvmExecutionReport> jvmExecutionReports = jUnit5TestsResult.getJvmExecutionReports();
        assertThat(jvmExecutionReports).as(errorReport)
                                       .extracting("executedTest")
                                       .containsExactlyInAnyOrder("test_method() in class invocation 1 with apple"
                                                                , "test_method() in class invocation 1 with banana");
        assertThatEachTestWasExecutedInItsOwnNewJvm(jvmExecutionReports);
        assertThat(errorReport).doesNotContain("another display name");

    }

    // The test JVM reports the execution of a new JVM for the test that this new JVM executed: the test of the class
    // invocation #n reports its display name with the value n
    private static void assertThatEachTestExecutedItsOwnInvocation(List<JvmExecutionReport> jvmExecutionReports) {
        for (JvmExecutionReport jvmExecutionReport : jvmExecutionReports) {
            String testUniqueId = jvmExecutionReport.getTestUniqueId();
            Matcher classInvocationIndex = CLASS_INVOCATION_INDEX.matcher(testUniqueId);
            assertThat(classInvocationIndex.find()).as("Class invocation index in %s", testUniqueId).isTrue();
            String expectedExecutedTest = jvmExecutionReport.getTestDisplayName()
                                        + " with value " + classInvocationIndex.group(1);
            assertThat(jvmExecutionReport.getExecutedTest())
                    .as("Test executed in the new JVM of %s", testUniqueId)
                    .isEqualTo(expectedExecutedTest);
        }
    }

}
