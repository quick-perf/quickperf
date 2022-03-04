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
package org.quickperf.jvm.jfr.annotation;

import java.lang.annotation.Annotation;

/**
 * <p>This class helps to build JDK Flight Recording (JFR) annotations with a global scope.</p>
 *
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 *scopes</i></a>
 *@see org.quickperf.config.SpecifiableGlobalAnnotations
 */
public class JfrAnnotationBuilder {

    private JfrAnnotationBuilder() { }

    /**
     *Allows to build {@link ProfileJvm} annotation.
     */
    public static ProfileJvm profileJvm() {
        return new ProfileJvm() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ProfileJvm.class;
            }
        };
    }

    /**
     *Allows to build {@link ExpectNoJvmIssue} annotation.
     */
    public static ExpectNoJvmIssue expectNoJvmIssue() {
        return new ExpectNoJvmIssue() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectNoJvmIssue.class;
            }
            @Override
            public int score() {
                return 60;
            }
        };
    }

    /**
     *Allows to build {@link ExpectNoJvmIssue} annotation.
     */
    public static ExpectNoJvmIssue expectNoJvmIssue(final int score) {
        return new ExpectNoJvmIssue() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectNoJvmIssue.class;
            }
            @Override
            public int score() {
                return score;
            }
        };
    }

}
