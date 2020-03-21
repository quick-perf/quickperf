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

package org.quickperf.measure;

public class BooleanMeasure extends AbstractComparablePerfMeasure<BooleanMeasure> {

    public static final BooleanMeasure TRUE = new BooleanMeasure(true);

    public static final BooleanMeasure FALSE = new BooleanMeasure(false);

    private static final String NO_COMMENT = "";

    private final Boolean value;

    private final String comment;

    public BooleanMeasure(Boolean value) {
        this.value = value;
        this.comment = NO_COMMENT;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public Void getUnit() {
        return null;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(BooleanMeasure otherBooleanMeasure) {
        return this.getValue().compareTo(otherBooleanMeasure.getValue());
    }

}
