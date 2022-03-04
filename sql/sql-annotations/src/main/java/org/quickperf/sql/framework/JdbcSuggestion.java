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
 * Copyright 2019-2022 the original author or authors.
 */
package org.quickperf.sql.framework;

import org.quickperf.SystemProperties;

public enum JdbcSuggestion implements QuickPerfSuggestion {

    SERVER_ROUND_TRIPS {
        @Override
        public String getMessage() {
            String bomb = "\uD83D\uDCA3";
            return    bomb + " " + "You may have even more select statements with production data."
                    + System.lineSeparator()
                    + "Be careful with the cost of JDBC roundtrips: "
                    + "https://blog.jooq.org/2017/12/18/the-cost-of-jdbc-server-roundtrips/";
        }
    },

    BATCHING {
        @Override
        public String getMessage() {
            if(SystemProperties.SIMPLIFIED_SQL_DISPLAY.evaluate()) {
                return "";
            }
            if (  ClassPath.INSTANCE.containsSpringDataJpa()
               && ClassPath.INSTANCE.containsSpringBoot() ) {
                return SpringDataJpaSpringBootSuggestion.BATCHING.getMessage();
            }
            if (ClassPath.INSTANCE.containsHibernate()) {
                return HibernateSuggestion.BATCHING.getMessage();
            }
            return "";
        }
    }

}
