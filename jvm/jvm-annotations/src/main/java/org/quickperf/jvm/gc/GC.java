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

package org.quickperf.jvm.gc;

import org.quickperf.testlauncher.JvmOption;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public enum GC {

      DEFAULT(Collections.singletonList(JvmOption.NONE))

    , EPSILON_GC(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                       , JvmOption.ALWAYS_PRE_TOUCH
                       , new JvmOption("-XX:+UseEpsilonGC")
                       )
                )

    , Z_GC(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                 , new JvmOption("-XX:+UseZGC")
                 )
          )

    , SHENANDOAH(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                       , new JvmOption("-XX:+UseZGC")
                       )
                )
    ;

    private final List<JvmOption> jvmOptions;

    GC(List<JvmOption> jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public List<JvmOption> getJvmOptions() {
        return jvmOptions;
    }

}