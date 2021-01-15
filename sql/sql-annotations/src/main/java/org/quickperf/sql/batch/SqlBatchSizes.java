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

package org.quickperf.sql.batch;

import org.quickperf.measure.PerfMeasure;
import org.quickperf.unit.CountUnit;

import java.io.Serializable;

/*
* Different batch sizes of batch executions.
*/
class SqlBatchSizes implements PerfMeasure<int[], CountUnit>, Serializable {

    private static final String NO_COMMENT = "";

    private final int[] measuredBatchSizes;

    SqlBatchSizes(int[] measuredBatchSizes) {
        this.measuredBatchSizes = measuredBatchSizes;
    }

    @Override
    public int[] getValue() {
        return measuredBatchSizes;
    }

    @Override
    public CountUnit getUnit() {
        return CountUnit.COUNT;
    }

    @Override
    public String getComment() {
        return NO_COMMENT;
    }

}
