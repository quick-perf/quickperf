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

package org.quickperf.sql.select.analysis;

import org.quickperf.measure.PerfMeasure;
import org.quickperf.unit.Count;
import org.quickperf.unit.NoUnit;

public class SelectAnalysis implements PerfMeasure {

    private final Count selectNumber;

    private final boolean sameSelectTypesWithDifferentParamValues;

    private final boolean sameSelects;

    public SelectAnalysis(int selectNumber
                        , boolean sameSelectTypesWithDifferentParamValues
                        , boolean sameSelects) {
        this.selectNumber = new Count(selectNumber);
        this.sameSelects = sameSelects;
        this.sameSelectTypesWithDifferentParamValues = sameSelectTypesWithDifferentParamValues;
    }

    public Count getSelectNumber() {
        return selectNumber;
    }

    public boolean hasSameSelectTypesWithDifferentParamValues() {
        return sameSelectTypesWithDifferentParamValues;
    }

    public boolean hasSameSelects() {
        return sameSelects;
    }

    @Override
    public SelectAnalysis getValue() {
        return this;
    }

    @Override
    public NoUnit getUnit() {
        return NoUnit.INSTANCE;
    }

    @Override
    public String getComment() {
        return null;
    }
}
