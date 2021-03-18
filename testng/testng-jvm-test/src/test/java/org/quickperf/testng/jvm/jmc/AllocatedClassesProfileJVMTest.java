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
