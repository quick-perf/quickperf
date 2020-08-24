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

package org.quickperf;

public class SystemProperties {

    private SystemProperties() {}

    public static final SystemProperty<String> WORKING_FOLDER =
            new SystemProperty<String>() {

                private final String name = "quickPerfWorkingFolder";

                @Override
                public String evaluate() {
                    return System.getProperty(name);
                }

                @Override
                public String buildForJvm(String workingFolderPath) {
                    return "-D" + name + "=" + workingFolderPath;
                }
            };

    public static final SystemProperty<Boolean> TEST_CODE_EXECUTING_IN_NEW_JVM =
            new SystemProperty<Boolean>() {

                private final String name = "quickPerfToExecInASpecificJvm";

                @Override
                public Boolean evaluate() {
                    String booleanAsString = System.getProperty(name);
                    return Boolean.valueOf(booleanAsString);
                }

                @Override
                public String buildForJvm(String propertyValue) {
                    return "-D" + name + "=" + propertyValue;
                }
            };

    public static final SystemProperty<Boolean> QUICK_PERF_DISABLED =
            new SystemProperty<Boolean>() {

                private final String name = "disableQuickPerf";

                @Override
                public Boolean evaluate() {
                    String booleanAsString = System.getProperty(name);
                    return Boolean.valueOf(booleanAsString);
                }

                @Override
                public String buildForJvm(String propertyValue) {
                    return "-D" + name + "=" + propertyValue;
                }
            };

    public static final SystemProperty<Boolean> SIMPLIFIED_SQL_DISPLAY =
            new SystemProperty<Boolean>() {

                private final String name = "limitQuickPerfSqlInfoOnConsole";

                @Override
                public Boolean evaluate() {
                    String booleanAsString = System.getProperty(name);
                    return Boolean.valueOf(booleanAsString);
                }

                @Override
                public String buildForJvm(String propertyValue) {
                    return "-D" + name + "=" + propertyValue;
                }
            };

    public static final SystemProperty<Boolean> SIMPLIFIED_JVM_PROFILE_DISPLAY =
            new SystemProperty<Boolean>() {

                private final String name = "limitQuickPerfJvmInfoOnConsole";

                @Override
                public Boolean evaluate() {
                    String booleanAsString = System.getProperty(name);
                    return Boolean.valueOf(booleanAsString);
                }

                @Override
                public String buildForJvm(String propertyValue) {
                    return "-D" + name + "=" + propertyValue;
                }
            };
}
