/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.jvm.jmcrule;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.util.IPreferenceValueProvider;
import org.openjdk.jmc.flightrecorder.rules.IRule;
import org.openjdk.jmc.flightrecorder.rules.Result;
import org.openjdk.jmc.flightrecorder.rules.RuleRegistry;
import org.openjdk.jmc.flightrecorder.rules.Severity;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.jvm.jfr.JfrRecording;
import org.quickperf.unit.Count;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

public class JmcRuleCountMeasureExtractor implements ExtractablePerformanceMeasure<JfrRecording, JmcRulesMeasure> {

    public static final JmcRuleCountMeasureExtractor INSTANCE = new JmcRuleCountMeasureExtractor();

    private JmcRuleCountMeasureExtractor() {}

    @Override
    public JmcRulesMeasure extractPerfMeasureFrom(JfrRecording jfrRecording) {

        IItemCollection jfrEvents = jfrRecording.getJfrEvents();
        List<Result> ruleEvaluations = evaluateJmcRules(jfrEvents);

        List<Count> jmcRules = buildJmcRuleCountsFrom(ruleEvaluations);

        return new JmcRulesMeasure(jmcRules);

    }

    private List<Result> evaluateJmcRules(IItemCollection jfrEvents) {
        List<Result> ruleEvaluations = new ArrayList<>();
        for (IRule rule : RuleRegistry.getRules()) {
            RunnableFuture<Result> future = rule.evaluate(jfrEvents, IPreferenceValueProvider.DEFAULT_VALUES);
            future.run();
            Result result;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
            ruleEvaluations.add(result);
        }
        return ruleEvaluations;
    }

    private List<Count> buildJmcRuleCountsFrom(List<Result> ruleEvaluations) {
        List<Count> jmcRules = new ArrayList<>();
        for (Result ruleEvaluation : ruleEvaluations) {
            Count ruleScore = buildJmcRuleCountFrom(ruleEvaluation);
            if(!ruleToExclude(ruleScore)) {
                jmcRules.add(ruleScore);
            }
        }
        return jmcRules;
    }

    private boolean ruleToExclude(Count ruleScore) {
        String ruleDescription = ruleScore.getComment();
        return     ruleDescription.contains("Rule: TLAB Allocation Ratio")
                || ruleDescription.contains("Rule: Competing Processes")
                || ruleDescription.contains("Rule: Command Line Options Check")
                || ruleDescription.contains("Rule: Metaspace Live Set Trend");
    }

    private Count buildJmcRuleCountFrom(Result result) {
        StringWriter stringWriter = new StringWriter();

        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println("Rule: " + result.getRule().getName());
        printWriter.println("Severity: " + Severity.get(result.getScore()));
        long score = (long) result.getScore();
        printWriter.println("Score: " + score);
        String longDescriptionAsHtml = result.getLongDescription();
        String textDesc = HtmlToPlainTextTransformer.INSTANCE.convertHtmlToPlainText(longDescriptionAsHtml);
        printWriter.println("Message: " + textDesc);

        String description = stringWriter.toString();
        return new Count(score, description);
    }

}
