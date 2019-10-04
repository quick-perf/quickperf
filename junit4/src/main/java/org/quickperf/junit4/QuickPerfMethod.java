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
 * Copyright 2019-2019 the original author or authors.
 */

package org.quickperf.junit4;

import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.runners.model.FrameworkMethod;
import org.quickperf.TestExecutionContext;
import org.quickperf.perfrecording.RecordablePerformance;

import java.lang.reflect.Method;
import java.util.List;

public class QuickPerfMethod extends FrameworkMethod {

    private final Method method;
    private final List<RecordablePerformance> perfRecordersToExecuteBeforeTestMethod;
    private final List<RecordablePerformance> perfRecordersToExecuteAfterTestMethod;
    private final TestExecutionContext testExecutionContext;

    public QuickPerfMethod(Method method
                         , TestExecutionContext testExecutionContext) {
        super(method);
        this.method = method;
        this.testExecutionContext = testExecutionContext;
        this.perfRecordersToExecuteBeforeTestMethod = testExecutionContext.getPerfRecordersToExecuteBeforeTestMethod();
        this.perfRecordersToExecuteAfterTestMethod = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
    }

    public Object invokeExplosively(final Object target, final Object... params)
            throws Throwable {
        return new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                startRecordings();
                try {
                    return method.invoke(target, params);
                } finally {
                    stopRecordings();
                }
            }
        }.run();
    }

    private void startRecordings() {
        for (int i = 0; i < perfRecordersToExecuteBeforeTestMethod.size(); i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteBeforeTestMethod.get(i);
            recordablePerformance.startRecording(testExecutionContext);
        }
    }

    private void stopRecordings() {
        for (int i = 0; i < perfRecordersToExecuteAfterTestMethod.size() ; i++) {
            RecordablePerformance recordablePerformance = perfRecordersToExecuteAfterTestMethod.get(i);
            recordablePerformance.stopRecording(testExecutionContext);
        }
    }

}
