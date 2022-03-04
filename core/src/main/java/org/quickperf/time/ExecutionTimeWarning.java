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
package org.quickperf.time;

class ExecutionTimeWarning {

    static final ExecutionTimeWarning INSTANCE = new ExecutionTimeWarning();

    private ExecutionTimeWarning() { }

    @Override
    public String toString() {
        String warning = "\u26A0";
        String tilde = "\u007E";
        return    System.lineSeparator() + warning + " " + "Be cautious with this result. It is a rough and first level result."
                + System.lineSeparator() + "Data has no meaning below the " + tilde + " second/millisecond."
                + System.lineSeparator() + "JIT warm-up, GC, or safe point scan impact the measure and its reproducibility."
                + System.lineSeparator() + "We recommend JMH to do more in-depth experiments: https://openjdk.java.net/projects/code-tools/jmh.";
    }

}
