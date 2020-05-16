package org.quickperf.sql.framework;

public enum SpringDataJpaSpringBootSuggestion implements QuickPerfSuggestion {

    BATCHING{
        @Override
        public String getMessage() {
            return    System.lineSeparator()
                    + System.lineSeparator()
                    + "\t* With Spring Boot and Spring Data JPA, you may fix it by adding in your application.properties file: "
                    + System.lineSeparator()
                    + "\tspring.jpa.properties.hibernate.jdbc.batch_size=\"...\" in your application.properties file."
                    + System.lineSeparator()
                    + "\tspring.jpa.properties.hibernate.order_inserts=true"
                    + System.lineSeparator()
                    + "\tspring.jpa.properties.hibernate.order_updates = true"
                    + System.lineSeparator()
                    + "\tIn case of versioned entities, you can also add:"
                    + System.lineSeparator()
                    + "\tspring.jpa.properties.hibernate.jdbc.batch_versioned_data=true"
                    ;
        }
    }
}
