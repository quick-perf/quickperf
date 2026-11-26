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
package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.ProfileConnection;
import org.quickperf.sql.display.DisplaySqlRecorder;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InvalidAnnotationValueTest {

    // The stack depth is checked only when the stack trace is displayed. The recorder of @DisplaySql, declared first, is
    // built first: it registers itself when it is built.
    @QuickPerfUnderTest
    static class DisplaySqlAndAnInvalidStackDepth {

        static final AtomicInteger TEST_METHOD_EXECUTIONS = new AtomicInteger();

        @DisplaySql
        @ProfileConnection(displayStackTrace = true, stackDepth = -2)
        @Test
        public void a_test() {
            TEST_METHOD_EXECUTIONS.incrementAndGet();
        }

    }

    @Test
    public void should_fail_a_test_with_an_invalid_annotation_value_and_release_the_recorders_already_built()
            throws NoSuchMethodException {

        // GIVEN: QuickPerf builds the recorders in the order of the annotations, the recorder of @DisplaySql first. If
        // it were built after the failure, it could never be left registered, and the test would always pass.
        Annotation[] annotations = DisplaySqlAndAnInvalidStackDepth.class.getMethod("a_test").getAnnotations();
        assertThat(annotations[0]).as("first annotation of the test method").isInstanceOf(DisplaySql.class);
        int testMethodExecutionsBefore = DisplaySqlAndAnInvalidStackDepth.TEST_METHOD_EXECUTIONS.get();

        // WHEN: on this thread, as the test
        JUnit5TestsResult result = JUnit5Tests.createInstance(DisplaySqlAndAnInvalidStackDepth.class).run();

        // THEN
        assertThat(result.getNumberOfFailures()).isOne();
        assertThat(result.getErrorReport()).contains("IllegalArgumentException")
                                           .contains("The stackDepth of @ProfileConnection should be a positive value");
        assertThat(DisplaySqlAndAnInvalidStackDepth.TEST_METHOD_EXECUTIONS.get() - testMethodExecutionsBefore)
                .as("executions of the test method")
                .isZero();
        assertThat(SqlRecorderRegistry.INSTANCE.getSqlRecorderOfType(DisplaySqlRecorder.class))
                .as("recorder of @DisplaySql left registered on the thread of the test")
                .isNull();

    }

}
