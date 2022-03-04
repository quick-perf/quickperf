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
package org.quickperf.jvm.gc;

import org.quickperf.testlauncher.JvmOption;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

/** Garbage Collector type**/
public enum GC {

    DEFAULT(Collections.singletonList(JvmOption.NONE))

    ,
    /**
     * See <a href= "https://openjdk.java.net/jeps/318">Epsilon GC - Doc 1</a> and
     * <a href= "https://shipilev.net/jvm/diy-gc/#_epsilon_gc">Epsilon GC - Doc 2</a>
     */
    EPSILON_GC(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                       , JvmOption.ALWAYS_PRE_TOUCH
                       , new JvmOption("-XX:+UseEpsilonGC")
                       )
                )

    ,
    /**
     *See <a href= "https://wiki.openjdk.java.net/display/zgc/Main">ZGC documentation</a>
     */
    ZGC(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                 , new JvmOption("-XX:+UseZGC")
                 )
          )

    ,
    /**
     *See <a href= "https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah documentation</a>
     */
    SHENANDOAH(asList( JvmOption.UNLOCK_EXPERIMENTAL_VM_OPTIONS
                       , new JvmOption("-XX:+UseShenandoahGC")
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
