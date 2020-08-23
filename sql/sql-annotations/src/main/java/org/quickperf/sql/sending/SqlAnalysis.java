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

package org.quickperf.sql.sending;

import org.quickperf.measure.PerfMeasure;
import org.quickperf.sql.select.analysis.SelectAnalysis;
import org.quickperf.unit.Count;
import org.quickperf.unit.NoUnit;

public class SqlAnalysis implements PerfMeasure {

    private final Count queriesSendingNumber;

    private final SelectAnalysis selectAnalysis;

    public SqlAnalysis(Count queriesSendingNumber, SelectAnalysis selectAnalysis) {
        this.queriesSendingNumber = queriesSendingNumber;
        this.selectAnalysis = selectAnalysis;
    }

    @Override
    public SqlAnalysis getValue() {
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

    public Count getQueriesSendingNumber() {
        return queriesSendingNumber;
    }

    public SelectAnalysis getSelectAnalysis() {
        return selectAnalysis;
    }

}
