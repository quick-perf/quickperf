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

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.issue.TestIssue;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.issue.PerfIssuesEvaluator;
import org.quickperf.issue.PerfIssuesToFormat;
import org.quickperf.perfrecording.TestThreads;
import org.quickperf.reporter.QuickPerfReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.lang.reflect.Method;
import java.util.Collection;

public class MainJvmAfterJUnitStatement extends Statement {

    private final NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final QuickPerfReporter quickPerfReporter = QuickPerfReporter.INSTANCE;

    private final FrameworkMethod frameworkMethod;

    private final Class<?> testClass;

    private final TestExecutionContext testExecutionContext;

    private final SetOfAnnotationConfigs testAnnotationConfigs;

    private final Statement junitAfters;

    public MainJvmAfterJUnitStatement(
              FrameworkMethod frameworkMethod
            , Class<?> testClass
            , TestExecutionContext testExecutionContext
            , QuickPerfConfigs quickPerfConfigs
            , Statement junitAfters) {
        this.testExecutionContext = testExecutionContext;
        this.frameworkMethod = frameworkMethod;
        this.testClass = testClass;
        this.testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        this.junitAfters = junitAfters;
    }

    @Override
    public void evaluate() throws Throwable {

        JvmOrTestIssue jvmOrTestIssue = evaluateBusinessOrTechnicalIssue();

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat =
                perfIssuesEvaluator.evaluatePerfIssuesIfNoJvmIssue(testAnnotationConfigs
                                                                 , testExecutionContext
                                                                 , jvmOrTestIssue);

        testExecutionContext.cleanResources();

        quickPerfReporter.report(jvmOrTestIssue
                               , groupOfPerfIssuesToFormat
                               , testExecutionContext);

    }

    private JvmOrTestIssue evaluateBusinessOrTechnicalIssue() {
        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            Method testMethod = frameworkMethod.getMethod();
            return newJvmTestLauncher.executeTestMethodInNewJvm(testClass
                                                              , testMethod
                                                              , testExecutionContext
                                                              , QuickPerfJunit4Core.class);

        }
        TestIssue testIssue = evaluateInSameJvm(junitAfters);
        return JvmOrTestIssue.buildFrom(testIssue);
    }

    private TestIssue evaluateInSameJvm(Statement junitAfters) {
        // This statement wraps the @Before methods, the test method and the
        // @After methods. Under a Timeout rule, JUnit executes it on a thread
        // started for the test, not on the thread that built the test
        // execution context. testExecutionContext.cleanResources(), called
        // afterwards on the same thread, unbinds the recorders from it.
        TestThreads.markCurrentThread();
        testExecutionContext.bindRecordersToCurrentThread();
        // A rule retrying the test evaluates this statement again: an abort of a @Before method must not be taken
        // for an abort thrown after the test method of a previous attempt
        testExecutionContext.setTestMethodCompleted(false);
        try {
            junitAfters.evaluate();
            return TestIssue.NONE;
        } catch (Throwable throwable) {
            if (testExecutionContext.isTestMethodCompleted()) {
                // Thrown by an @After method, for example: the performance recording of the test method is complete
                return TestIssue.buildFromThrowableAfterTestMethod(throwable);
            }
            // Thrown by a @Before method or by the test method, for example
            return TestIssue.buildFrom(throwable);
        }
    }

}
