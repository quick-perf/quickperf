package org.quickperf.jvm.jmc.value.allocationbyclass;

import org.openjdk.jmc.flightrecorder.rules.Result;

public class AllocationByClassResult {

    private final Result result;

    public AllocationByClassResult(Result result) {
        this.result = result;
    }

    public Result getResult() {
        return result;
    }
}
