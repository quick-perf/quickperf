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

/**
 * <p>This class helps to build core annotations with a global scope.</p>
 *
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 *scopes</i></a>
 *@see org.quickperf.config.SpecifiableGlobalAnnotations
 */
public class CoreAnnotationBuilder {

    private CoreAnnotationBuilder() {}

    /**
     *Allows to build {@link org.quickperf.annotation.DebugQuickPerf} annotation.
     */
    public static DebugQuickPerf debugQuickPerf() {
        return new DebugQuickPerf() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DebugQuickPerf.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.annotation.DisableGlobalAnnotations} annotation.
     */
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

    /**
     *Allows to build {@link org.quickperf.annotation.DisableGlobalAnnotations} annotation.
     */
    public static DisableGlobalAnnotations disableGlobalAnnotations() {
        String comment = "";
        return disableGlobalAnnotations(comment);
    }

    /**
     *Allows to build {@link org.quickperf.annotation.DisableQuickPerf} annotation.
     */
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

    /**
     *Allows to build {@link org.quickperf.annotation.DisableQuickPerf} annotation.
     */
    public static DisableQuickPerf disableQuickPerf() {
        return disableQuickPerf("");
    }

    /**
     *Allows to build {@link org.quickperf.annotation.FunctionalIteration} annotation.
     */
    public static FunctionalIteration functionalIteration() {
        return new FunctionalIteration() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return FunctionalIteration.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.annotation.DisplayAppliedAnnotations} annotation.
     */
    public static DisplayAppliedAnnotations displayAppliedAnnotations() {
        return new DisplayAppliedAnnotations() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DisplayAppliedAnnotations.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.annotation.MeasureExecutionTime} annotation.
     */
    public static MeasureExecutionTime measureExecutionTime() {
        return new MeasureExecutionTime() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MeasureExecutionTime.class;
            }
        };
    }

    /**
     *Allows to build {@link org.quickperf.annotation.ExpectMaxExecutionTime} annotation.
     */
    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfHours(final int hours) {
        return expectMaxExecutionTime(hours, 0, 0, 0);
    }

    /**
     *Allows to build {@link org.quickperf.annotation.ExpectMaxExecutionTime} annotation.
     */
    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfMinutes(final int minutes) {
        return expectMaxExecutionTime(0, minutes, 0, 0);
    }

    /**
     *Allows to build {@link org.quickperf.annotation.ExpectMaxExecutionTime} annotation.
     */
    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfSeconds(final int seconds) {
        return expectMaxExecutionTime(0, 0, seconds, 0);
    }

    /**
     *Allows to build {@link org.quickperf.annotation.ExpectMaxExecutionTime} annotation.
     */
    public static ExpectMaxExecutionTime expectMaxExecutionTimeOfMilliSeconds(final int ms) {
        return expectMaxExecutionTime(0, 0, 0, ms);
    }

    /**
     *Allows to build {@link org.quickperf.annotation.ExpectMaxExecutionTime} annotation.
     */
    public static ExpectMaxExecutionTime expectMaxExecutionTime( final int hours, final int minutes
                                                               , final int seconds, final int milliSeconds) {
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
            public Class<? extends Annotation> annotationType() {
                return ExpectMaxExecutionTime.class;
            }
        };
    }

}
