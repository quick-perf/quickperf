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
package org.quickperf.jvm.allocation;

public class JvmAllocationUnitFormatter {

    public static final JvmAllocationUnitFormatter INSTANCE = new JvmAllocationUnitFormatter();

    private JvmAllocationUnitFormatter() {}

    public String format(AllocationUnit allocationUnit) {
        if(AllocationUnit.MEGA_BYTE.equals(allocationUnit)) {
            return "m";
        } else if (AllocationUnit.KILO_BYTE.equals(allocationUnit)) {
            return "k";
        } else if (AllocationUnit.GIGA_BYTE.equals(allocationUnit)) {
            return "g";
        }
        return "";
    }
}
