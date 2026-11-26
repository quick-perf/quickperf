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
package org.quickperf.junit6.global;

import org.junit.jupiter.api.Test;
import org.quickperf.annotation.DisableGlobalAnnotations;
import org.quickperf.annotation.DisableQuickPerf;
import org.quickperf.annotation.FunctionalIteration;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.JvmExecutionReport;
import org.quickperf.junit6.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.HeapSize;
import org.quickperf.sql.config.MemoryDataSourceBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder.aDataSourceBuilder;

// QuickPerfConfigurationWithAJvmAnnotation specifies @HeapSize and @DisableLikeWithLeadingWildcard as global
// annotations. quick-perf-junit6 executes the tests in the test JVM: it ignores the global @HeapSize with a warning, and
// applies the global SQL annotation.
class GlobalJvmAnnotationJUnit6Test {

    private static final DataSource DATA_SOURCE =
            aDataSourceBuilder().buildProxy(MemoryDataSourceBuilder.aDataSource().build());

    private static final String WARNING = "[QUICK PERF] The QuickPerf JUnit 6 extension (quick-perf-junit6) does not"
                                        + " apply the global annotation @HeapSize of"
                                        + " org.quickperf.QuickPerfConfigurationWithAJvmAnnotation: it needs QuickPerf"
                                        + " to start a dedicated JVM for each test, but the extension executes every"
                                        + " test in the JVM running JUnit.";

    @QuickPerfTest
    static class TestsWithTheGlobalAnnotations {

        @Test
        void execute_a_like_with_a_leading_wildcard() {
            executeSql("select 'quickperf' like '%perf'");
        }

        @Test
        void report_its_jvm() {
            throw JvmExecutionReport.reportExecutionOf("report_its_jvm");
        }

    }

    @Test
    void should_apply_the_global_sql_annotation_and_not_the_global_jvm_annotation() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestsWithTheGlobalAnnotations.class).run();

        // THEN
        String errorReport = result.getErrorReport();
        assertThat(result.getNumberOfFailures()).as(errorReport).isEqualTo(2);
        assertThat(errorReport).contains("Like with leading wildcard")
                               .doesNotContain("ExtensionConfigurationException");
        JvmExecutionReport.assertThatEachTestWasExecutedInThisJvm(result.getJvmExecutionReports());
        assertThat(quickPerfReportEntriesOf(result)).containsExactly(WARNING);

    }

    @QuickPerfTest
    static class PassingTests {

        @Test
        void a_test() { }

        @Test
        void another_test() { }

        @Test
        void a_third_test() { }

    }

    @QuickPerfTest
    static class OtherPassingTests {

        @Test
        void a_test() { }

        @Test
        void another_test() { }

    }

    @Test
    void should_publish_the_warning_once_per_launch() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(Collections.emptyMap()
                                                            , PassingTests.class
                                                            , OtherPassingTests.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(5);
        assertThat(quickPerfReportEntriesOf(result)).containsExactly(WARNING);

    }

    @Test
    void should_publish_the_warning_once_per_launch_executing_the_tests_concurrently() {

        // GIVEN
        Map<String, String> concurrentExecution = new HashMap<>();
        concurrentExecution.put("junit.jupiter.execution.parallel.enabled", "true");
        concurrentExecution.put("junit.jupiter.execution.parallel.mode.default", "concurrent");
        concurrentExecution.put("junit.jupiter.execution.parallel.mode.classes.default", "concurrent");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.strategy", "fixed");
        concurrentExecution.put("junit.jupiter.execution.parallel.config.fixed.parallelism", "4");

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(concurrentExecution
                                                            , PassingTests.class
                                                            , OtherPassingTests.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(5);
        assertThat(quickPerfReportEntriesOf(result)).containsExactly(WARNING);

    }

    @QuickPerfTest
    @DisableGlobalAnnotations
    static class TestsWithoutTheGlobalAnnotations {

        @Test
        void execute_a_like_with_a_leading_wildcard() {
            executeSql("select 'quickperf' like '%perf'");
        }

    }

    @Test
    void should_not_warn_about_global_annotations_that_do_not_apply() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestsWithoutTheGlobalAnnotations.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(quickPerfReportEntriesOf(result)).isEmpty();

    }

    @QuickPerfTest
    static class TestsWithQuickPerfDisabled {

        @DisableQuickPerf
        @Test
        void a_test_with_quickperf_disabled() { }

        @FunctionalIteration
        @Test
        void a_test_of_a_functional_iteration() { }

    }

    @Test
    void should_not_warn_about_the_global_annotations_of_a_test_with_quickperf_disabled() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(TestsWithQuickPerfDisabled.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).as(result.getErrorReport()).isZero();
        assertThat(result.getNumberOfSucceededTests()).isEqualTo(2);
        assertThat(quickPerfReportEntriesOf(result)).isEmpty();

    }

    @QuickPerfTest
    static class HeapSizeOnTheTestMethod {

        @HeapSize(value = 30, unit = AllocationUnit.MEGA_BYTE)
        @Test
        void a_test() { }

    }

    // The annotation of the test method replaces the global annotation of the same type
    @Test
    void should_fail_a_test_whose_method_has_an_annotation_of_the_type_of_the_global_jvm_annotation() {

        // WHEN
        JUnit5TestsResult result = JUnit5Tests.createInstance(HeapSizeOnTheTestMethod.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains("ExtensionConfigurationException")
                                           .contains("@HeapSize needs QuickPerf to start a dedicated JVM");
        assertThat(quickPerfReportEntriesOf(result)).isEmpty();

    }

    private static List<String> quickPerfReportEntriesOf(JUnit5TestsResult result) {
        return result.getReportEntries()
                     .stream()
                     .filter(reportEntry -> reportEntry.containsKey("QuickPerf"))
                     .map(reportEntry -> reportEntry.get("QuickPerf"))
                     .collect(Collectors.toList());
    }

    private static void executeSql(String sql) {
        try (Connection connection = DATA_SOURCE.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeQuery(sql).close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
