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

package org.quickperf.sql.select.analysis;

import org.quickperf.SystemProperties;
import org.quickperf.measure.PerfMeasure;
import org.quickperf.sql.framework.ClassPath;
import org.quickperf.sql.framework.HibernateSuggestion;
import org.quickperf.sql.framework.JdbcSuggestion;
import org.quickperf.sql.framework.MicronautSuggestion;
import org.quickperf.unit.Count;
import org.quickperf.unit.NoUnit;

public class SelectAnalysis implements PerfMeasure {

    private final Count selectNumber;

    private final SameSelectTypesWithDifferentParamValues sameSelectTypesWithDifferentParamValues;

    private final Count sameSelectsNumber;

    public static class SameSelectTypesWithDifferentParamValues {

        private final boolean value;

        public SameSelectTypesWithDifferentParamValues(boolean value) {
            this.value = value;
        }

        public boolean evaluate() {
            return value;
        }

        public String getSuggestionToFixIt() {
            return getSuggestionToFIxNPlusOneSelect();
        }

    }

    public static String getSuggestionToFIxNPlusOneSelect() {

        if(SystemProperties.SIMPLIFIED_SQL_DISPLAY.evaluate()) {
            return "";
        }

        String suggestion =  System.lineSeparator()
                           + System.lineSeparator()
                           + JdbcSuggestion.SERVER_ROUND_TRIPS.getMessage();

        if(ClassPath.INSTANCE.containsHibernate()) {
            suggestion += System.lineSeparator()
                        + HibernateSuggestion.N_PLUS_ONE_SELECT.getMessage();
        }

        if(ClassPath.INSTANCE.containsMicronautData()) {
            suggestion += System.lineSeparator()
                        + MicronautSuggestion.N_PLUS_ONE_SELECT.getMessage();
        }

        return suggestion;

    }

    public SelectAnalysis(int selectNumber
                        , int sameSelectsNumber
                        , boolean sameSelectTypesWithDifferentParamValues) {
        this.selectNumber = new Count(selectNumber);
        this.sameSelectsNumber = new Count(sameSelectsNumber);
        this.sameSelectTypesWithDifferentParamValues = new SameSelectTypesWithDifferentParamValues(sameSelectTypesWithDifferentParamValues);
    }

    public Count getSelectNumber() {
        return selectNumber;
    }

    public boolean hasOnlySameSelects() {
        return selectNumber.isEqualTo(sameSelectsNumber);
    }

    public SameSelectTypesWithDifferentParamValues getSameSelectTypesWithDifferentParamValues() {
        return sameSelectTypesWithDifferentParamValues;
    }

    public boolean hasSameSelects() {
        return sameSelectsNumber.isGreaterThan(Count.ZERO);
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
