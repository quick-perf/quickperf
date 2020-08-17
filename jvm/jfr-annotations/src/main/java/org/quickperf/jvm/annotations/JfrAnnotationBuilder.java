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

package org.quickperf.jvm.annotations;

import java.lang.annotation.Annotation;

public class JfrAnnotationBuilder {

    private JfrAnnotationBuilder() { }

    public static ProfileJvm profileJvm() {
        return new ProfileJvm() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return ProfileJvm.class;
            }
        };
    }

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
