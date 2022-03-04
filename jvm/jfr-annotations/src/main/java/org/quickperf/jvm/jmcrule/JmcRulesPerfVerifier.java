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
package org.quickperf.jvm.jmcrule;

import org.quickperf.issue.PerfIssue;
import org.quickperf.issue.VerifiablePerformanceIssue;
import org.quickperf.jvm.jfr.annotation.ExpectNoJvmIssue;
import org.quickperf.unit.Count;

import java.util.ArrayList;
import java.util.List;

public class JmcRulesPerfVerifier implements VerifiablePerformanceIssue<ExpectNoJvmIssue, JmcRulesMeasure> {

    public static final VerifiablePerformanceIssue INSTANCE = new JmcRulesPerfVerifier();

    private JmcRulesPerfVerifier() { }

    @Override
    public PerfIssue verifyPerfIssue(ExpectNoJvmIssue annotation, JmcRulesMeasure jmcRules) {
        int maxExpectedScore = annotation.score();
        List<Count> jmcRulesAsCount = jmcRules.getValue();

        if (atLeastOneRuleHasScoreGreaterThanExpected(jmcRulesAsCount, maxExpectedScore)) {
            List<Count> jmcRulesWithValueGreaterThanExpected = retrieveJmcRulesWithValueGreaterThanExpected(jmcRulesAsCount, maxExpectedScore);
            return buildPerfIssueFrom(jmcRulesWithValueGreaterThanExpected
                                    , maxExpectedScore);
        }

        return PerfIssue.NONE;
    }

    private boolean atLeastOneRuleHasScoreGreaterThanExpected(List<Count> jmcRulesAsCount
            , int maxExpectedScore) {
        for (Count jmcRule : jmcRulesAsCount) {
            if (jmcRule.getValue() >= maxExpectedScore) {
                return true;
            }
        }
        return false;
    }

    private PerfIssue buildPerfIssueFrom(List<Count> jmcRulesWithValueGreaterThanExpected, int maxExpectedScore) {
        String assertionMessage = "JMC rules are expected to have score less than <" + maxExpectedScore + ">.";
        StringBuilder perfIssueDesc = new StringBuilder(assertionMessage);
        perfIssueDesc.append(System.lineSeparator());
        perfIssueDesc.append(System.lineSeparator());
        for (Count jmcRule : jmcRulesWithValueGreaterThanExpected) {
            perfIssueDesc.append(jmcRule.getComment());
            perfIssueDesc.append(System.lineSeparator());
        }
        return new PerfIssue(perfIssueDesc.toString());
    }

    private List<Count> retrieveJmcRulesWithValueGreaterThanExpected(List<Count> jmcRulesAsCount, int maxExpectedScore) {
        List<Count> jmcRulesWithValueGreaterThanExpected = new ArrayList<>();
        for (Count jmcRule : jmcRulesAsCount) {
            if (jmcRule.getValue() >= maxExpectedScore) {
                jmcRulesWithValueGreaterThanExpected.add(jmcRule);
            }
        }
        return jmcRulesWithValueGreaterThanExpected;
    }

}
