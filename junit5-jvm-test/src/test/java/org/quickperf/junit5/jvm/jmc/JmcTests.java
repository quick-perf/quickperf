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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.allocation.AllocationUnit;
import org.quickperf.jvm.annotations.ExpectNoJvmIssue;
import org.quickperf.jvm.annotations.HeapSize;

import java.util.ArrayList;
import java.util.List;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class JmcTests {

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

        // WHEN
        TestExecutionSummary printableResult = testResult(testClass);

        // THEN
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(printableResult.getFailures().size())
                      .isEqualTo(1);
        String cause = printableResult.getFailures().get(0).getException().getMessage();
        softAssertions.assertThat(cause)
                      .contains("JMC rules are expected to have score less than <50>.")
                      .contains("Rule: Primitive To Object Conversion");
        softAssertions.assertAll();

    }

    private TestExecutionSummary testResult(Class<?> testClass) {
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        return listener.getSummary();
    }

}
