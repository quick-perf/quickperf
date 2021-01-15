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

package org.quickperf.unit;

import org.quickperf.measure.AbstractComparablePerfMeasure;

public class Count extends AbstractComparablePerfMeasure<Count> {

    public static final Count ZERO = new Count(0);

    public static final Count ONE = new Count(1);

    private static final String NO_COMMENT = "";

    private final Long value;

    private final CountUnit unit;

    private final String comment;

    public Count(Integer value) {
        this.value = value.longValue();
        this.unit = CountUnit.COUNT;
        this.comment = NO_COMMENT;
    }

    public Count(Long value, String comment) {
        this.value = value;
        this.unit = CountUnit.COUNT;
        this.comment = comment;
    }

    public Count(Long value) {
        this.value = value;
        this.unit = CountUnit.COUNT;
        this.comment = NO_COMMENT;
    }

    @Override
    public Long getValue() {
        return value;
    }

    @Override
    public CountUnit getUnit() {
        return unit;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(Count otherCount) {
        return this.getValue().compareTo(otherCount.getValue());
    }

}