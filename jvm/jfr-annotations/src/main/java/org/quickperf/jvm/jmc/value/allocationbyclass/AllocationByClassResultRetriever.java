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

package org.quickperf.jvm.jmc.value.allocationbyclass;

import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.util.IPreferenceValueProvider;
import org.openjdk.jmc.flightrecorder.rules.Result;
import org.openjdk.jmc.flightrecorder.rules.jdk.memory.AllocationByClassRule;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;

public class AllocationByClassResultRetriever {
    public static final AllocationByClassResultRetriever INSTANCE = new AllocationByClassResultRetriever();
    private static final AllocationByClassRule allocationByClassRule = new AllocationByClassRule();
    private static final Result NO_RESULT = new Result(allocationByClassRule, 0d, "");

    private AllocationByClassResultRetriever() {}

    public AllocationByClassResult extractAllocationByClassResultFrom(IItemCollection jfrEvents) {
        RunnableFuture<Result> allocationByClassRuleFuture =
                allocationByClassRule.evaluate(jfrEvents, IPreferenceValueProvider.DEFAULT_VALUES);
        allocationByClassRuleFuture.run();
        Result result;
        try {
            result = allocationByClassRuleFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            result = NO_RESULT;
        }
        return new AllocationByClassResult(result);
    }
}
