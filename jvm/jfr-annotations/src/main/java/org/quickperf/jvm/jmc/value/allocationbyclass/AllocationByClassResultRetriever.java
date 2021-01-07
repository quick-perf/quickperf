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
