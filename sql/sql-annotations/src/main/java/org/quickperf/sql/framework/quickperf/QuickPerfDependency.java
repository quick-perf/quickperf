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

package org.quickperf.sql.framework.quickperf;

public enum QuickPerfDependency {

      QUICKPERF_SPRING_BOOT_1_SQL_STARTER("quick-perf-springboot1-sql-starter")
    , QUICKPERF_SPRING_BOOT_2_SQL_STARTER("quick-perf-springboot2-sql-starter")
    , QUICKPERF_SQL_SPRING_4("quick-perf-sql-spring4")
    , QUICKPERF_SQL_SPRING_5("quick-perf-sql-spring5");

    private final String groupId = "org.quickperf";

    private final String artifactId;

    public String getArtifactId() {
        return artifactId;
    }

    QuickPerfDependency(String artifactId) {
        this.artifactId = artifactId;
    }

    String toMavenWithVersion() {
        return  "\t\t<dependency>" + System.lineSeparator() +
                "\t\t\t<groupId>" + groupId + "</groupId>" + System.lineSeparator() +
                "\t\t\t<artifactId>" + artifactId + "</artifactId>" + System.lineSeparator() +
                "\t\t\t<scope>test</scope>" + System.lineSeparator() +
                "\t\t</dependency>";
    }

    String toGradleWithVersion() {
        return "\t\ttestCompile group: '" + groupId + "'"
                + ", name: '" + artifactId + "'";
    }

    String getMavenSearchLink() {
        return "https://search.maven.org/search?q=a:" + artifactId;
    }

}