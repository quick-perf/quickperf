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

import org.quickperf.sql.framework.ClassPath;
import org.quickperf.sql.framework.QuickPerfSuggestion;

import static org.quickperf.sql.framework.quickperf.QuickPerfDependency.*;

class SpringDataSourceConfig implements QuickPerfSuggestion {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private final ClassPath classPath;

    SpringDataSourceConfig(ClassPath classPath) {
        this.classPath = classPath;
    }

    @Override
    public String getMessage() {

        if (classPath.containsSpringBoot1()) {
            if (classPath.contains(QUICKPERF_SPRING_BOOT_STARTER_1)) {
                return    buildDataJpaTestAnnotationMessage()
                        + LINE_SEPARATOR
                        + LINE_SEPARATOR + buildSpringRESTControllerMessage();
            }
            return "To configure it, add the following dependency: "
                  + LINE_SEPARATOR + format(QUICKPERF_SPRING_BOOT_STARTER_1);
        }

        if (classPath.containsSpringBoot2()) {
            if (classPath.contains(QUICKPERF_SPRING_BOOT_STARTER_2)) {
                return    buildDataJpaTestAnnotationMessage()
                        + LINE_SEPARATOR
                        + LINE_SEPARATOR + buildSpringRESTControllerMessage();
            }
            return   "To configure it, add the following dependency: "
                    + LINE_SEPARATOR + format(QUICKPERF_SPRING_BOOT_STARTER_2);
        }

        if (classPath.containsSpring4()) {
            if (classPath.contains(QUICKPERF_SQL_SPRING_4)) {
                return    "Import QuickPerfSpringConfig:"
                        + LINE_SEPARATOR + buildImportQuickPerfSpringConfigExample()
                        + LINE_SEPARATOR
                        + LINE_SEPARATOR + buildSpringRESTControllerMessage();
            }
            return   "To configure the proxy, add the following dependency: "
                    + LINE_SEPARATOR + format(QUICKPERF_SQL_SPRING_4)
                    + LINE_SEPARATOR + "You have also to import QuickPerfSpringConfig:"
                    + LINE_SEPARATOR + buildImportQuickPerfSpringConfigExample();
        }

        if (classPath.containsSpring5()) {
            if (classPath.contains(QUICKPERF_SQL_SPRING_5)) {
                return    "Import QuickPerfSpringConfig:"
                        + LINE_SEPARATOR + buildImportQuickPerfSpringConfigExample()
                        + LINE_SEPARATOR
                        + LINE_SEPARATOR + buildSpringRESTControllerMessage();
            }
            return  "To configure the proxy, add the following dependency: "
                    + LINE_SEPARATOR + format(QUICKPERF_SQL_SPRING_5)
                    + LINE_SEPARATOR + "You have also to import QuickPerfSpringConfig:"
                    + LINE_SEPARATOR + buildImportQuickPerfSpringConfigExample();
        }

        return "";

    }

    private String buildDataJpaTestAnnotationMessage() {
        return    "Do you use @DataJpaTest? This annotation disables Spring auto-configuration."
                + LINE_SEPARATOR + "So, QuickPerf Spring auto-configuration is disabled."
                + LINE_SEPARATOR + "To allow QuickPerf to intercept the SQL queries, you have two possibilities: "
                + LINE_SEPARATOR + "1) Import QuickPerfSpringConfig class (recommended): "
                + LINE_SEPARATOR + buildImportQuickPerfSpringConfigExample()
                + LINE_SEPARATOR + "2) Force to enable Spring auto-configuration by adding"
                + LINE_SEPARATOR + "   " + "@OverrideAutoConfiguration(enabled = true) on the test class";
    }

    private String buildSpringRESTControllerMessage() {
        return                     "Are you testing a REST controller without MockMvc? Execute the test in"
                + LINE_SEPARATOR + "a dedicated JVM by adding" + " @HeapSize(value = ..., unit = AllocationUnit.MEGA_BYTE)."
                + LINE_SEPARATOR + "A heap size value around 50 megabytes may allow the test to run.";
    }

    private String buildImportQuickPerfSpringConfigExample() {
        return                     "\timport org.quickperf.spring.sql.QuickPerfSpringConfig;"
                + LINE_SEPARATOR + "\t..."
                + LINE_SEPARATOR + "\t@Import(QuickPerfSpringConfig.class)"
                + LINE_SEPARATOR + "\tpublic class TestClass {";
    }


    public String format(QuickPerfDependency quickPerfDependency) {
        return    "\t* Maven"
                + LINE_SEPARATOR
                + quickPerfDependency.toMavenWithVersion()
                + LINE_SEPARATOR
                + "\t* Gradle"
                + LINE_SEPARATOR
                + quickPerfDependency.toGradleWithVersion()
                + LINE_SEPARATOR
                + "\t* Other: " + quickPerfDependency.getMavenSearchLink()
                ;
    }

}