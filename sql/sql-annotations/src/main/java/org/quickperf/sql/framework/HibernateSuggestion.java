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

package org.quickperf.sql.framework;

public enum HibernateSuggestion implements QuickPerfSuggestion {

    N_PLUS_ONE_SELECT() {

        @Override
        public String getMessage() {
            String lightBulb = "\uD83D\uDCA1";
            String message =  System.lineSeparator()
                    + lightBulb + " Perhaps you are facing a N+1 select issue"
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
            if(SqlFrameworksInClassPath.INSTANCE.containsSpringDataJpa()) {
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
            String lightBulb = "\uD83D\uDCA1";

            return System.lineSeparator()
                    + lightBulb + " Perhaps you may think that JDBC batching is enabled."
                    + System.lineSeparator()
                    + "\t* You can verify it using @ExpectJdbcBatching"
                    + System.lineSeparator()
                    + "\t Sometimes you may think that JDBC is enabled but in fact not:"
                    + System.lineSeparator()
                    + "\t https://abramsm.wordpress.com/2008/04/23/hibernate-batch-processing-why-you-may-not-be-using-it-even-if-you-think-you-are/"
                    + System.lineSeparator()
                    + "\t https://stackoverflow.com/questions/27697810/hibernate-disabled-insert-batching-when-using-an-identity-identifier"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tYou should check that you project has the following Hibernate properties"
                    + System.lineSeparator()
                    + "\thibernate.jdbc.batch_size => positive value"
                    + System.lineSeparator()
                    + "\thibernate.order_inserts => true"
                    + System.lineSeparator()
                    + "\thibernate.order_updates => true"
                    + System.lineSeparator()
                    + "\tIn case of versioned entities, you should also check"
                    + "\thibernate.jdbc.batch_versioned_data => true"
                    ;
        }
    },

    SESSION() {

        @Override
        public String getMessage() {
            return "Exactly same select statements may show a bad use of Hibernate session.";
        }

    }

}
