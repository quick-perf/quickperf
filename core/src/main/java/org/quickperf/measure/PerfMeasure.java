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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.measure;

import org.quickperf.perfrecording.PerfRecord;

public interface PerfMeasure<V, U> extends PerfRecord {

    PerfMeasure NONE = new PerfMeasure() {

        final Object value = new Object();

        final Object unit = new Object();

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getUnit() {
            return unit;
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public boolean equals(Object obj) {
            return obj == NONE;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return "No perf measure.";
        }

    };

    V getValue();

    U getUnit();

    String getComment();

}
