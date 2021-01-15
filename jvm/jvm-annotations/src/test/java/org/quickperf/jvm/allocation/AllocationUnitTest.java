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

package org.quickperf.jvm.allocation;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AllocationUnitTest {

    @Test public void
    should_get_value_in_bytes() {
        long numberOfBytesInGigabytes = AllocationUnit.GIGA_BYTE.getValueInBytes();
        long expected = 1073741824;
        assertThat(numberOfBytesInGigabytes).isEqualTo(expected);
    }

}