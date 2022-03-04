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

import org.quickperf.sql.annotation.DisplaySql;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;

public enum HibernateSuggestion implements QuickPerfSuggestion {

    N_PLUS_ONE_SELECT() {

        @Override
        public String getMessage() {
            String lightBulb = "\uD83D\uDCA1";
            String message =  System.lineSeparator()
                    + lightBulb + " Perhaps you are facing an N+1 select issue"
                    + System.lineSeparator()
                    + "\t* With Hibernate, you may fix it by using JOIN FETCH"
                    + System.lineSeparator()
                    + "\t                                       or LEFT JOIN FETCH"
                    + System.lineSeparator()
                    + "\t                                       or FetchType.LAZY"
                    + System.lineSeparator()
                    + "\t                                       or ..."
                    + System.lineSeparator()
                    + "\t  Some examples: https://stackoverflow.com/questions/32453989/what-is-the-solution-for-the-n1-issue-in-jpa-and-hibernate\";"
                    + System.lineSeparator()
                    + "\t                 https://stackoverflow.com/questions/52850442/how-to-get-rid-of-n1-with-jpa-criteria-api-in-hibernate/52945771?stw=2#52945771"
                    + System.lineSeparator()
                    + "\t                 https://thoughts-on-java.org/jpa-21-entity-graph-part-1-named-entity/";
            if(ClassPath.INSTANCE.containsSpringDataJpa()) {
                message +=  System.lineSeparator()
                        +   System.lineSeparator()
                        +   "\t* With Spring Data JPA, you may fix it by adding @EntityGraph(attributePaths = { \"...\" })"
                        +   System.lineSeparator()
                        +   "\t  on repository method: " + "https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph";
            }
            return message;
        }

    },

    BATCHING() {

        @Override
        public String getMessage() {

            return System.lineSeparator()
                    + "\tYou should check that hibernate.jdbc.batch_size Hibernate property has a positive value."
                    + System.lineSeparator()
                    + "\tA batch size value between 5 and 30 is generally recommended."
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tNote that IDENTITY generator disables JDBC batching: https://stackoverflow.com/questions/27697810/hibernate-disabled-insert-batching-when-using-an-identity-identifier"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tOrdering inserts and updates is recommended. To do this, set hibernate.order_inserts and hibernate.order_updates Hibernate properties with true."
                    + System.lineSeparator()
                    + "\tThis paper explains why: https://abramsm.wordpress.com/2008/04/23/hibernate-batch-processing-why-you-may-not-be-using-it-even-if-you-think-you-are/"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tWith versioned entities, you should also set hibernate.jdbc.batch_versioned_data Hibernate property with true. "
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tUse @" + DisplaySqlOfTestMethodBody.class.getSimpleName()
                    + " or @" + DisplaySql.class.getSimpleName() + " to display JDBC executions.";
        }
    },

}
