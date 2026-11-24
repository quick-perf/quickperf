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
package org.quickperf.junit4;

import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runners.model.FrameworkMethod;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.perfrecording.PerformanceRecording;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class QuickPerfMethod extends FrameworkMethod {

    private final Method method;

    private final TestExecutionContext testExecutionContext;

    private final PerformanceRecording performanceRecording = PerformanceRecording.INSTANCE;

    // Test.None if the test method does not expect an exception with @Test(expected = ...)
    private final Class<? extends Throwable> expectedException;

    public QuickPerfMethod(Method method
                        , TestExecutionContext testExecutionContext) {
        super(method);
        this.method = method;
        this.testExecutionContext = testExecutionContext;
        Test test = method.getAnnotation(Test.class);
        this.expectedException = test == null ? Test.None.class : test.expected();
    }

    public Object invokeExplosively(final Object target, final Object... params)
            throws Throwable {
        return new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                boolean testMethodCompleted = false;
                performanceRecording.start(testExecutionContext);
                try {
                    Object result = method.invoke(target, params);
                    testMethodCompleted = true;
                    return result;
                } catch (InvocationTargetException invocationTargetException) {
                    // The test method completed as expected by @Test(expected = ...). A failing assumption does not
                    // complete it, even if it is an instance of the expected exception: JUnit 4.11 and 4.12 then
                    // report the test as aborted, other versions as passed.
                    Throwable testMethodThrowable = invocationTargetException.getTargetException();
                    testMethodCompleted =    expectedException.isInstance(testMethodThrowable)
                                          && !(testMethodThrowable instanceof AssumptionViolatedException);
                    throw invocationTargetException;
                } finally {
                    performanceRecording.stop(testExecutionContext);
                    // Once the recording is stopped, so that it does not measure the allocations of this call
                    recordCompletion(testMethodCompleted);
                }
            }
        }.run();
    }

    // An assumption failing once the test method has completed, in an @After method for example, leaves the
    // performance recording complete: QuickPerf then evaluates it, unlike when the assumption fails before the end of
    // the test method
    private void recordCompletion(boolean testMethodCompleted) {
        testExecutionContext.setTestMethodCompleted(testMethodCompleted);
        if (SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
            TestMethodCompletionInNewJvm.INSTANCE.record(testMethodCompleted);
        }
    }

}
