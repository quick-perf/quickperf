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
package org.quickperf.testlauncher;

import java.util.Objects;

public class JvmOption {

    public static final JvmOption NONE = new JvmOption("");

    public static final JvmOption UNLOCK_EXPERIMENTAL_VM_OPTIONS =
            new JvmOption("-XX:+UnlockExperimentalVMOptions");

    public static final JvmOption ALWAYS_PRE_TOUCH = new JvmOption("-XX:+AlwaysPreTouch");
    
    private final String valueAsString;

    public JvmOption(String valueAsString) {
        this.valueAsString = valueAsString;
    }

    @Override
    public String toString() {
        return valueAsString;
    }

    public String asString() {
        return valueAsString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JvmOption jvmOption = (JvmOption) o;

        return Objects.equals(valueAsString, jvmOption.valueAsString);
    }

    @Override
    public int hashCode() {
        return valueAsString != null ? valueAsString.hashCode() : 0;
    }

}
