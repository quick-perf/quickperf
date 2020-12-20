package org.quickperf.testng.jvm.jmc;

import org.quickperf.jvm.jfr.annotation.ProfileJvm;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class AllocatedClassesProfileJVMTest {

    @ProfileJvm
    @Test
    public void mostAllocatedClassesProfileJVMAnnotation() {
        IntegerAccumulator integerAccumulator = new IntegerAccumulator();
        integerAccumulator.accumulateInteger(500_000);
    }

    private static class IntegerAccumulator {

        private List<Integer> integerList;

        void accumulateInteger(int numberOfIntegers) {
            integerList = new ArrayList<>(numberOfIntegers);
            for (int i = 1; i <= numberOfIntegers; i++) {
                integerList.add(i);
            }
        }

    }
}
