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

package org.quickperf.config;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * To apply annotations to each QuickPerf test, you can configure the annotations with a global scope. To do this, implement the <code>SpecifiableGlobalAnnotation</code>s interface
 * and locate the implementation in an <code>org.quickperf</code> package.
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf#annotation-scopes"><i>QuickPerf annotations
 *scopes</i></a>
 *@see <a href="https://github.com/quick-perf/doc/wiki/QuickPerf-test"><i>QuickPerf test</i></a>
 */
public interface SpecifiableGlobalAnnotations {

    /**
     * Allows to define the annotations applying to each QuickPerf test.
     * @return the annotations applying to each QuickPerf test
     */
    Collection<Annotation> specifyAnnotationsAppliedOnEachTest();

}
