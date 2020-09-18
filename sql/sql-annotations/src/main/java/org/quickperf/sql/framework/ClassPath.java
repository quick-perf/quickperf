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

package org. quickperf.sql.framework;

import org.quickperf.sql.framework.quickperf.QuickPerfDependency;

public class ClassPath {

    public static final ClassPath INSTANCE = new ClassPath();

    private final String value;

    private ClassPath() {
        this.value = System.getProperty("java.class.path");
    }

    public String getValue() {
        return value;
    }

    public boolean containsHibernate() {
        return value.contains("hibernate-core");
    }

    public boolean containsSpringDataJpa() {
        return value.contains("spring-data-jpa");
    }

    public boolean containsSpringBoot() {
        return     value.contains("org.springframework.boot")
                || value.contains("spring-boot");
    }

    public boolean containsSpringBoot1() {
        return value.contains("spring-boot-1");
    }

    public boolean containsSpringBoot2() {
        return value.contains("spring-boot-2");
    }

    public boolean containsSpringCore() {
        return containsSpringframework() && value.contains("spring-core");
    }

    private boolean containsSpringframework() {
        return value.contains("springframework");
    }

    public boolean containsSpring4() {
        return containsSpringframework() && value.contains("spring-core-4");
    }

    public boolean containsSpring5() {
        return containsSpringframework() && value.contains("spring-core-5");
    }

    public boolean containsQuarkusCore() {
        return value.contains("quarkus-core");
    }

    public boolean containsMicronautData() {
        return value.contains("micronaut-data");
    }

    public boolean containsMicronautHibernateJpa() {
        return value.contains("micronaut-hibernate-jpa");
    }

    public boolean contains(QuickPerfDependency quickPerfDependency) {
        String quickPerfArtifactId = quickPerfDependency.getArtifactId();
        return value.contains(quickPerfArtifactId);
    }

}
