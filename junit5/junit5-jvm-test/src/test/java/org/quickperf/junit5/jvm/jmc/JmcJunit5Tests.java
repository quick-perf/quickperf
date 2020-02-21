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

package org.quickperf.junit5.jvm.jmc;

import org.junit.jupiter.api.Test;
import org.quickperf.junit5.JUnit5Tests;
import org.quickperf.junit5.JUnit5Tests.JUnit5TestsResult;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectNoJvmIssue;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JmcJunit5Tests {

    @QuickPerfTest
    public static class ClassWithFailingJmcRules {

        private static class IntegerAccumulator {

            private List<Integer> integerList;

            void accumulateInteger(int numberOfIntegers) {
                integerList = new ArrayList<>(numberOfIntegers);
                for (int i = 1; i <= numberOfIntegers; i++) {
                    integerList.add(i);
                }
            }

        }

        @HeapSize(value = 100, unit = AllocationUnit.MEGA_BYTE)
        @ExpectNoJvmIssue(score = 50)
        @Test
        public void code_with_scores_of_jmc_rules_greater_than_50() {

            IntegerAccumulator integerAccumulator = new IntegerAccumulator();
            integerAccumulator.accumulateInteger(3_000_000);

        }

    }

    @Test public void
    jmc_rules_having_score_greater_than_expected() {

        // GIVEN
        Class<?> testClass = ClassWithFailingJmcRules.class;
        JUnit5Tests jUnit5Tests = JUnit5Tests.createInstance(testClass);

        // WHEN
        JUnit5TestsResult jUnit5TestsResult = jUnit5Tests.run();

        // THEN
        assertThat(jUnit5TestsResult.getNumberOfFailures()).isOne();

        String errorReport = jUnit5TestsResult.getErrorReport();
        assertThat(errorReport).contains("JMC rules are expected to have score less than <50>.")
                               .contains("Rule: Primitive To Object Conversion");

    }

}
