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

package org.quickperf.sql.framework;

public enum HibernateSuggestion implements QuickPerfSuggestion {

    N_PLUS_ONE_SELECT() {

        @Override
        public String getMessage() {
            String message =  System.lineSeparator()
                            + "Perhaps you are facing a N+1 select issue"
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
                            + "\t                 https://thoughts-on-java.org/jpa-21-entity-graph-part-1-named-entity/";
            if(SqlFrameworksInClassPath.INSTANCE.containsSpringDataJpa()) {
                message +=  System.lineSeparator()
                          + System.lineSeparator()
                          + "\t* With Spring Data JPA, you may fix it by adding"
                          + System.lineSeparator()
                          + "\t@EntityGraph(attributePaths = { \"...\" }) on repository method."
                          + System.lineSeparator()
                          + "\thttps://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.entity-graph";
            }
            return message;
        }

    },

    SESSION() {

        @Override
        public String getMessage() {
            return "Exactly same select requests may show a bad use of Hibernate session.";
        }

    }

}
