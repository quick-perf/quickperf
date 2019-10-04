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

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.quickperf.*;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.SetOfAnnotationConfigs;
import org.quickperf.perfrecording.RecordablePerformance;
import org.quickperf.reporter.ConsoleReporter;
import org.quickperf.testlauncher.NewJvmTestLauncher;

import java.util.Collection;
import java.util.List;

public class MainJvmAfterJUnitStatement extends Statement {

    private final FrameworkMethod frameworkMethod;

    private final TestExecutionContext testExecutionContext;

    private final SetOfAnnotationConfigs testAnnotationConfigs;

    private final Statement junitAfters;

    private final IssueThrower issueThrower = IssueThrower.INSTANCE;

    private final NewJvmTestLauncher newJvmTestLauncher = NewJvmTestLauncher.INSTANCE;

    private final JUnit4FailuresRepository jUnit4FailuresRepository = JUnit4FailuresRepository.getInstance();

    private final PerfIssuesEvaluator perfIssuesEvaluator = PerfIssuesEvaluator.INSTANCE;

    private final ConsoleReporter consoleReporter = ConsoleReporter.INSTANCE;

    public MainJvmAfterJUnitStatement(
              FrameworkMethod frameworkMethod
            , TestExecutionContext testExecutionContext
            , QuickPerfConfigs quickPerfConfigs
            , Statement junitAfters) {
        this.testExecutionContext = testExecutionContext;
        this.frameworkMethod = frameworkMethod;
        this.testAnnotationConfigs = quickPerfConfigs.getTestAnnotationConfigs();
        this.junitAfters = junitAfters;
    }

    @Override
    public void evaluate() throws Throwable {

        Throwable businessThrowable = null;

        if (testExecutionContext.testExecutionUsesTwoJVMs()) {
            newJvmTestLauncher.run( frameworkMethod.getMethod()
                                  , testExecutionContext.getWorkingFolder()
                                  , testExecutionContext.getJvmOptions()
                                  , QuickPerfJunit4Core.class);
            WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
            businessThrowable = jUnit4FailuresRepository.find(workingFolder);
        } else {
            // Run test in same jvm
            try {
                junitAfters.evaluate();
            } catch (Throwable throwable) {
                businessThrowable = throwable;
            }
        }

        Collection<PerfIssuesToFormat> groupOfPerfIssuesToFormat = perfIssuesEvaluator.evaluatePerfIssues(testAnnotationConfigs, testExecutionContext, jUnit4FailuresRepository);

        cleanResources();

        if(testExecutionContext.areQuickPerfAnnotationsToBeDisplayed()) {
            consoleReporter.displayQuickPerfAnnotations(testExecutionContext.getPerfAnnotations());
        }

        if (testExecutionContext.isQuickPerfDebugMode()) {
            consoleReporter.displayQuickPerfDebugInfos();
        }

        issueThrower.throwIfNecessary(businessThrowable, groupOfPerfIssuesToFormat);

    }

    private void cleanResources() {
        List<RecordablePerformance> perfRecorders = testExecutionContext.getPerfRecordersToExecuteAfterTestMethod();
        for (RecordablePerformance perfRecorder : perfRecorders) {
            perfRecorder.cleanResources();
        }
    }

}
