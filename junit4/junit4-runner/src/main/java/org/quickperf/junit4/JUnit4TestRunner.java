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

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.quickperf.issue.BusinessOrTechnicalIssue;
import org.quickperf.testlauncher.TestRunnerFromMain;

import java.util.ArrayList;
import java.util.List;

class JUnit4TestRunner implements TestRunnerFromMain.FrameworkTestRunner {

    @Override
    public BusinessOrTechnicalIssue executeTestMethod(Class<?> testClass, String methodName) {

        Request junitRequestOfMethod = Request.method(testClass, methodName);

        Result testResult = new JUnitCore().run(junitRequestOfMethod);

        List<Failure> jUnit4Failures = testResult.getFailures();

        List<Throwable> jUnit4failuresAsThrowables = convertJUnit4FailuresToThrowables(jUnit4Failures);

        return  BusinessOrTechnicalIssue.buildFrom(jUnit4failuresAsThrowables);

    }

    private List<Throwable> convertJUnit4FailuresToThrowables(List<Failure> jUnit4Failures) {
        List<Throwable> throwables = new ArrayList<>();
        for (Failure failure : jUnit4Failures) {
            Throwable throwable = failure.getException();
            throwables.add(throwable);
        }
        return throwables;
    }

}
