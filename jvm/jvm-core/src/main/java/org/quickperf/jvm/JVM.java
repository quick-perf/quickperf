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
package org.quickperf.jvm;

public class JVM {

    public static final JVM INSTANCE = new JVM();

    public final Type type;

    public final Version version;

    public final String name;

    private JVM() {
        name = System.getProperty("java.vm.name");
        type = new Type(name);
        version = new Version();
    }

    @Override
    public String toString() {
        String javaVendor = System.getProperty("java.vendor");
        return    name + " (name)"
                + ", " + version.toString() + " (version)"
                + ", " + javaVendor + " (vendor)";
    }

    public static class Type {

        private final String jvmName;

        private Type(String jvmName) {
            this.jvmName = jvmName;
        }

        public boolean isOpenJdkJvm() {
            return jvmName.contains("OpenJDK");
        }

        public boolean isHotSpotJvm() {
            return jvmName.contains("HotSpot");
        }

    }

    public static class Version {

        private final String javaJvmSpecificationVersion = System.getProperty("java.vm.specification.version");

        private Version() { }

        public boolean is7() {
            return javaJvmSpecificationVersion.contains("1.7");
        }

        public boolean is8() {
            return javaJvmSpecificationVersion.contains("1.8");
        }

        public boolean isGreaterThanOrEqualTo9() {
            if (is7() || is8()) {
                return false;
            }
            return findJvmVersionAsInt() >= 9;
        }

        private int findJvmVersionAsInt() {
            return Integer.parseInt(javaJvmSpecificationVersion);
        }

        public boolean isGreaterThanOrEqualTo11() {
            return !is7() && !is8() && findJvmVersionAsInt() >= 11;
        }

        public boolean isGreaterThanOrEqualTo12() {
            return !is7() && !is8() && findJvmVersionAsInt() >= 12;
        }

        public boolean isGreaterThanOrEqualTo16() {
            return !is7() && !is8() && findJvmVersionAsInt() >= 16;
        }

        public boolean isLessThanTo16() {
            return !isGreaterThanOrEqualTo16();
        }

        @Override
        public String toString() {
            return System.getProperty("java.vm.version");
        }

    }

}
