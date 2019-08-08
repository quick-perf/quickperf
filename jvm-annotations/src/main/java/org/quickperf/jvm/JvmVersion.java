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

package org.quickperf.jvm;

public class JvmVersion {

    private static final String JAVA_VM_SPECIFICATION_VERSION_PROPERTY = "java.vm.specification.version";

    private JvmVersion() { }

    public static boolean is7() {
        String jvmVersionAsString = System.getProperty(JAVA_VM_SPECIFICATION_VERSION_PROPERTY);
        return jvmVersionAsString.contains("1.7");
    }

    public static boolean is8() {
        String jvmVersionAsString = System.getProperty(JAVA_VM_SPECIFICATION_VERSION_PROPERTY);
        return jvmVersionAsString.contains("1.8");
    }

    public static boolean isGreaterThanOrEqualTo9() {
        if (is7() || is8()) {
            return false;
        }
        return findJvmVersionAsInt() >= 9;
    }

    private static int findJvmVersionAsInt() {
        String jvmVersionAsString = System.getProperty(JAVA_VM_SPECIFICATION_VERSION_PROPERTY);
        return Integer.parseInt(jvmVersionAsString);
    }

    public static boolean isGreaterThanOrEqualTo11() {
        if (is7() || is8()) {
            return false;
        }
        return findJvmVersionAsInt() >= 11;
    }

    public static boolean isGreaterThanOrEqualTo12() {
        if (is7() || is8()) {
            return false;
        }
        return findJvmVersionAsInt() >= 12;
    }

}
