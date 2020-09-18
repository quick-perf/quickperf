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
