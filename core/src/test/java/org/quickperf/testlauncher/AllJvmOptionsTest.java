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
package org.quickperf.testlauncher;

import org.junit.Test;
import org.mockito.Mockito;
import org.quickperf.WorkingFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class AllJvmOptionsTest {

    @Test
    public void jvm_options_should_be_unique() {

        // GIVEN
        String flightRecorderOptionAsString = "-XX:+FlightRecorder";
        List<JvmOption> jvmOptions =
                Arrays.asList(  new JvmOption(flightRecorderOptionAsString)
                        , new JvmOption(flightRecorderOptionAsString)
                );

        // WHEN
        AllJvmOptions allJvmOptions = new AllJvmOptions.Builder()
                .addOptions(jvmOptions)
                .build();

        // THEN
        WorkingFolder workingFolder = Mockito.mock(WorkingFolder.class);
        assertThat(allJvmOptions.asStrings(workingFolder)).containsOnlyOnce(flightRecorderOptionAsString);

    }

    @Test public void
    should_respect_order_of_jvm_options() {

        // GIVEN
        List<JvmOption> jvmOptions = new ArrayList<>();
        jvmOptions.add(new JvmOption("-XX:+UnlockDiagnosticVMOptions"));
        jvmOptions.add(new JvmOption("-XX:+DebugNonSafepoints"));

        // WHEN
        AllJvmOptions allJvmOptions = new AllJvmOptions.Builder()
                                      .addOptions(jvmOptions)
                                      .build();

        // THEN
        WorkingFolder workingFolder = Mockito.mock(WorkingFolder.class);
        List<String> jvmOptionsAsString = allJvmOptions.asStrings(workingFolder);
        assertThat(jvmOptionsAsString.get(0)).isEqualTo("-XX:+UnlockDiagnosticVMOptions");
        assertThat(jvmOptionsAsString.get(1)).isEqualTo("-XX:+DebugNonSafepoints");

    }

}