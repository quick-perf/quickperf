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

public enum SpringDataJpaSpringBootSuggestion implements QuickPerfSuggestion {

    BATCHING{
        @Override
        public String getMessage() {

            return System.lineSeparator()
                    + "\tYou should check that spring.jpa.properties.hibernate.jdbc.batch_size property has a positive value in application.properties."
                    + System.lineSeparator()
                    + "\tA batch size value between 5 and 30 is generally recommended."
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tNote that IDENTITY generator disables JDBC batching: https://stackoverflow.com/questions/27697810/hibernate-disabled-insert-batching-when-using-an-identity-identifier"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tOrdering inserts and updates is recommended. To do this, add following properties in application.properties:"
                    + System.lineSeparator()
                    + "\t\t" + "spring.jpa.properties.hibernate.order_inserts=true"
                    + System.lineSeparator()
                    + "\t\t" + "spring.jpa.properties.hibernate.order_updates=true"
                    + System.lineSeparator()
                    + "\tThis paper explains why: https://abramsm.wordpress.com/2008/04/23/hibernate-batch-processing-why-you-may-not-be-using-it-even-if-you-think-you-are/"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tWith versioned entities, you should also add following property in application.properties: "
                    + System.lineSeparator()
                    + "\t\t" + "spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true"
                    + System.lineSeparator()
                    + System.lineSeparator()
                    + "\tUse @" + DisplaySqlOfTestMethodBody.class.getSimpleName()
                    + " or @" + DisplaySql.class.getSimpleName() + " to display JDBC executions.";
        }
    }
}
