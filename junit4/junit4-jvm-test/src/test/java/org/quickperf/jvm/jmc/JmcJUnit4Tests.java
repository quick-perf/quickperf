/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf.jvm.jmc;

import org.junit.Test;
import org.junit.experimental.results.PrintableResult;
import org.junit.runner.RunWith;
import org.quickperf.junit4.QuickPerfJUnitRunner;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectNoJvmIssue;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.experimental.results.PrintableResult.testResult;

public class JmcJUnit4Tests {

    @RunWith(QuickPerfJUnitRunner.class)
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

        // WHEN
        PrintableResult printableResult = testResult(testClass);

        // THEN
        assertThat(printableResult.failureCount()).isOne();

        assertThat(printableResult.toString())
                      .contains("JMC rules are expected to have score less than <50>.")
                      .contains("Rule: Primitive" +
                              " To Object Conversion");
    }

}
