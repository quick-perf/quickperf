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

package org.quickperf.jvm.jfr;

import org.quickperf.jvm.JVM;

class UnableToProfileJvmWithJdkFlightRecorder extends RuntimeException {

    UnableToProfileJvmWithJdkFlightRecorder() {
        super(buildMessage());
    }

    private static String buildMessage() {

        String pointingRight = "\uD83D\uDC49";

        return    System.lineSeparator()
                + "\t * JDK Flight Recorder profiling is available with "
                + System.lineSeparator()
                + "\t \t * OpenJDK JDK >= 11"
                + System.lineSeparator()
                + "\t\t * OpenJDK JDK 8 with a version greater than u262/u272 (following vendors)"
                + System.lineSeparator()
                + "\t\t   " + pointingRight + " Article from Marcus Hirt giving details: http://hirt.se/blog/?p=1235"
                + System.lineSeparator()
                + "\t\t * Oracle JDK >= 1.7u40"
                + System.lineSeparator()
                + "\t * Used JVM: " + JVM.INSTANCE.toString()
                ;
    }

}
