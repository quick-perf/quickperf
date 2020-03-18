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

package org.quickperf.sql.update.columns;

import org.quickperf.measure.PerfMeasure;
import org.quickperf.unit.NoUnit;

public class NumberOfUpdatedColumnsStatisticsMeasure implements PerfMeasure<NumberOfUpdatedColumnsStatistics, NoUnit> {

    private final NumberOfUpdatedColumnsStatistics numberOfUpdatedColumnsStatistics;

    private final String comment;

    public NumberOfUpdatedColumnsStatisticsMeasure(NumberOfUpdatedColumnsStatistics numberOfUpdatedColumnsStatistics
                                                 , String comment) {
        this.numberOfUpdatedColumnsStatistics = numberOfUpdatedColumnsStatistics;
        this.comment = comment;
    }

    @Override
    public NumberOfUpdatedColumnsStatistics getValue() {
        return numberOfUpdatedColumnsStatistics;
    }

    @Override
    public NoUnit getUnit() {
        return NoUnit.INSTANCE;
    }

    @Override
    public String getComment() {
        return comment;
    }

}
