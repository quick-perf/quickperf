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

import org.junit.Test;
import org.quickperf.jvm.annotations.JvmOptions;
import org.quickperf.testlauncher.JvmOption;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JvmOptionConverterTest {

    private final JvmOptionConverter jvmOptionConverter = JvmOptionConverter.INSTANCE;

    @Test public void
    should_return_jvm_option_objects_from_jvm_options_as_a_string() {

        // GIVEN
        JvmOptions jvmOptionsAsAnnotation = mock(JvmOptions.class);
        when(jvmOptionsAsAnnotation.value()).thenReturn(" -XX:+UseCompressedOops   -XX:+UseCompressedClassPointers  ");

        // WHEN
        List<JvmOption> jvmOptions = jvmOptionConverter.jvmOptionFrom(jvmOptionsAsAnnotation);

        // THEN
        assertThat(jvmOptions).hasSize(2);

        JvmOption firstJvmOption = jvmOptions.get(0);
        assertThat(firstJvmOption.asString()).isEqualTo("-XX:+UseCompressedOops");

        JvmOption secondJvmOption = jvmOptions.get(1);
        assertThat(secondJvmOption.asString()).isEqualTo("-XX:+UseCompressedClassPointers");

    }

}