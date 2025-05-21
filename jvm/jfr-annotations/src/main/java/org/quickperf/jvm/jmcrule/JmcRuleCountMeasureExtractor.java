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

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.common.util.IPreferenceValueProvider;
import org.openjdk.jmc.flightrecorder.rules.*;
import org.openjdk.jmc.flightrecorder.rules.jdk.cpu.HighJvmCpuRule;
import org.openjdk.jmc.flightrecorder.rules.jdk.memory.GarbageCollectionInfoRule;
import org.openjdk.jmc.flightrecorder.rules.jdk.memory.GcStallRule;
import org.openjdk.jmc.flightrecorder.rules.jdk.memory.HeapInspectionRule;
import org.openjdk.jmc.flightrecorder.rules.jdk.memory.SystemGcRule;
import org.quickperf.ExtractablePerformanceMeasure;
import org.quickperf.jvm.jfr.JfrRecording;
import org.quickperf.unit.Count;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.stream.Collectors;

public class JmcRuleCountMeasureExtractor implements ExtractablePerformanceMeasure<JfrRecording, JmcRulesMeasure> {

    public static final JmcRuleCountMeasureExtractor INSTANCE = new JmcRuleCountMeasureExtractor();
    private static final List<Class<? extends IRule>> IGNORED_RULES = Arrays.asList(
            HighJvmCpuRule.class,
            GcStallRule.class,
            GarbageCollectionInfoRule.class,
            HeapInspectionRule.class,
            SystemGcRule.class
    );

    private JmcRuleCountMeasureExtractor() {
    }

    @Override
    public JmcRulesMeasure extractPerfMeasureFrom(JfrRecording jfrRecording) {

        IItemCollection jfrEvents = jfrRecording.getJfrEvents();
        List<IResult> ruleEvaluations = evaluateJmcRules(jfrEvents);

        List<Count> jmcRules = buildJmcRuleCountsFrom(ruleEvaluations);

        return new JmcRulesMeasure(jmcRules);

    }

    private List<IResult> evaluateJmcRules(IItemCollection jfrEvents) {
        List<IResult> ruleEvaluations = new ArrayList<>();
        Collection<IRule> rules = RuleRegistry.getRules().stream()
                .filter(r -> !IGNORED_RULES.contains(r.getClass()))
                .collect(Collectors.toList());
        for (IRule rule : rules) {
            RunnableFuture<IResult> future = rule.createEvaluation(jfrEvents,
                    IPreferenceValueProvider.DEFAULT_VALUES, new ResultProvider());
            future.run();
            IResult result;
            try {
                result = future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
            ruleEvaluations.add(result);
        }
        return ruleEvaluations;
    }

    private List<Count> buildJmcRuleCountsFrom(List<IResult> ruleEvaluations) {
        List<Count> jmcRules = new ArrayList<>();
        for (IResult ruleEvaluation : ruleEvaluations) {
            Count ruleScore = buildJmcRuleCountFrom(ruleEvaluation);
            if (!ruleToExclude(ruleScore)) {
                jmcRules.add(ruleScore);
            }
        }
        return jmcRules;
    }

    private boolean ruleToExclude(Count ruleScore) {
        String ruleDescription = ruleScore.getComment();
        return ruleDescription.contains("Rule: TLAB Allocation Ratio")
               || ruleDescription.contains("Rule: Competing Processes")
               || ruleDescription.contains("Rule: Command Line Options Check")
               || ruleDescription.contains("Rule: Metaspace Live Set Trend");
    }

    private Count buildJmcRuleCountFrom(IResult result) {
        StringWriter stringWriter = new StringWriter();

        PrintWriter printWriter = new PrintWriter(stringWriter);
        printWriter.println("Rule: " + result.getRule().getName());
        printWriter.println("Severity: " + result.getSeverity());
        long score = Optional.ofNullable(result.getResult(TypedResult.SCORE))
                .map(IQuantity::longValue)
                .orElse(-1L);
        printWriter.println("Score: " + score);
        final String longDescriptionAsHtml = Optional.ofNullable(result.getSummary()).orElse("")
                + Optional.ofNullable(result.getExplanation()).orElse("")
                + Optional.ofNullable(result.getSolution()).orElse("");
        String textDesc = HtmlToPlainTextTransformer.INSTANCE.convertHtmlToPlainText(longDescriptionAsHtml);
        printWriter.println("Message: " + textDesc);

        String description = stringWriter.toString();
        return new Count(score, description);
    }

}