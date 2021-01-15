/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2021 the original author or authors.
 */

package org.quickperf.junit4;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runners.model.FrameworkMethod;
import org.quickperf.TestExecutionContext;
import org.quickperf.perfrecording.PerformanceRecording;

import java.lang.reflect.Method;

class QuickPerfMethod extends FrameworkMethod {

    private final Method method;

    private final TestExecutionContext testExecutionContext;

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    public QuickPerfMethod(Method method
                        , TestExecutionContext testExecutionContext) {
        super(method);
        this.method = method;
        this.testExecutionContext = testExecutionContext;
    }

    public Object invokeExplosively(final Object target, final Object... params)
            throws Throwable {
        return new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                performanceRecording.start(testExecutionContext);
                try {
                    return method.invoke(target, params);
                } finally {
                    performanceRecording.stop(testExecutionContext);
                }
            }
        }.run();
    }

}
