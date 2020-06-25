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

package org.quickperf.time;


import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ExecutionTimeComparisonTest {

    @Test public void
    one_hour_should_be_greater_than_one_second() {
        ExecutionTime oneHour = new ExecutionTime(1, TimeUnit.HOURS);
        ExecutionTime oneSecond = new ExecutionTime(1, TimeUnit.SECONDS);
        assertThat(oneHour.isGreaterThan(oneSecond)).isTrue();
    }

    @Test public void
    one_second_should_be_less_than_one_hour() {
        ExecutionTime oneSecond = new ExecutionTime(1, TimeUnit.SECONDS);
        ExecutionTime oneHour = new ExecutionTime(1, TimeUnit.HOURS);
        assertThat(oneSecond.isLessThan(oneHour)).isTrue();
    }

    @Test public void
    same() {
        ExecutionTime oneSecond = new ExecutionTime(1, TimeUnit.SECONDS);
        ExecutionTime oneSecondBis = new ExecutionTime(1, TimeUnit.SECONDS);
        assertThat(oneSecond.isEqualTo(oneSecondBis)).isTrue();
    }

}