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
package org.quickperf.junit6;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JvmAnnotationsTest {

    // The warning about the global annotations needing a dedicated JVM: Maven Surefire shows no report entry, so the
    // console is where Maven users see it, once per JVM
    @Test
    void should_print_the_warning_once() {

        // GIVEN
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        AtomicBoolean printed = new AtomicBoolean();

        // WHEN
        JvmAnnotations.printOnce("a warning", printStream, printed);
        JvmAnnotations.printOnce("a warning", printStream, printed);

        // THEN
        assertThat(output.toString(StandardCharsets.UTF_8)).isEqualTo("a warning" + System.lineSeparator());

    }

}
