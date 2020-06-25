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

package org.quickperf.annotation;

import java.lang.annotation.Annotation;

public class CoreAnnotationBuilder {

    private CoreAnnotationBuilder() {}

    public static DebugQuickPerf debugQuickPerf() {
        return new DebugQuickPerf() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DebugQuickPerf.class;
            }
        };
    }

    public static DisableGlobalAnnotations disableGlobalAnnotations(final String comment) {
        return new DisableGlobalAnnotations() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableGlobalAnnotations.class;
            }
            @Override
            public String comment() {
                return comment;
            }
        };
    }

    public static DisableGlobalAnnotations disableGlobalAnnotations() {
        String comment = "";
        return disableGlobalAnnotations(comment);
    }

    public static DisableQuickPerf disableQuickPerf(final String comment) {
        return new DisableQuickPerf() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisableQuickPerf.class;
            }
            @Override
            public String comment() {
                return comment;
            }
        };
    }

    public static DisableQuickPerf disableQuickPerf() {
        return disableQuickPerf("");
    }

    public static FunctionalIteration functionalIteration() {
        return new FunctionalIteration() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return FunctionalIteration.class;
            }
        };
    }

    public static DisplayAppliedAnnotations displayAppliedAnnotations() {
        return new DisplayAppliedAnnotations() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplayAppliedAnnotations.class;
            }
        };
    }

    public static MeasureExecutionTime measureExecutionTime() {
        return new MeasureExecutionTime() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MeasureExecutionTime.class;
            }
        };
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfHours(final int hours) {
        return expectMaxExecutionTime(hours, 0, 0, 0, 0, 0);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfMinutes(final int minutes) {
        return expectMaxExecutionTime(0, minutes, 0, 0, 0, 0);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfSeconds(final int seconds) {
        return expectMaxExecutionTime(0, 0, seconds, 0, 0, 0);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfMilliSeconds(final int ms) {
        return expectMaxExecutionTime(0, 0, 0, ms, 0, 0);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfMicroSeconds(final int microSeconds) {
        return expectMaxExecutionTime(0, 0, 0, 0, microSeconds, 0);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfNanoSeconds(final int nanoSeconds) {
        return expectMaxExecutionTime(0, 0, 0, 0, 0, nanoSeconds);
    }

    public static ExpectMaxExecutionTime expectMaxExecutionTime( final int hours, final int minutes
                                                               , final int seconds, final int milliSeconds
                                                               , final int microSeconds, final int nanoSeconds) {
        return new ExpectMaxExecutionTime() {
            @Override
            public int hours() {
                return hours;
            }
            @Override
            public int minutes() {
                return minutes;
            }
            @Override
            public int seconds() {
                return seconds;
            }
            @Override
            public int milliSeconds() {
                return milliSeconds;
            }
            @Override
            public long microSeconds() {
                return microSeconds;
            }
            @Override
            public long nanoSeconds() {
                return nanoSeconds;
            }
            @Override
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxExecutionTime.class;
            }
        };
    }

}
