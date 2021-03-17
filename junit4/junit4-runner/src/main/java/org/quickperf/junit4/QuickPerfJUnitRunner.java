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

import junit.runner.Version;
import org.junit.Test;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.config.library.QuickPerfConfigs;
import org.quickperf.config.library.QuickPerfConfigsLoader;
import org.quickperf.jvm.JVM;

import java.lang.reflect.Method;
import java.util.List;

public class QuickPerfJUnitRunner extends BlockJUnit4ClassRunner {

    private static final Statement NO_STATEMENT = new Statement() {
        @Override
        public void evaluate() {
        }
    };

    private final QuickPerfConfigs quickPerfConfigs = QuickPerfConfigsLoader.INSTANCE.loadQuickPerfConfigs();

    private TestExecutionContext testExecutionContext;

    public QuickPerfJUnitRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        validatePublicVoidNoArgMethods(Test.class, false, errors);
    }

    @Override
    public List<FrameworkMethod> computeTestMethods() {
        return getTestClass().getAnnotatedMethods(Test.class);
    }

    @Override
    public Statement methodInvoker(FrameworkMethod frameworkMethod, Object test) {
        Method testMethod = frameworkMethod.getMethod();

        int runnerAllocationOffset = findJUnit4AllocationOffset();

        testExecutionContext = TestExecutionContext.buildFrom(quickPerfConfigs
                                                            , testMethod
                                                            , runnerAllocationOffset);

        if(testExecutionContext.isQuickPerfDisabled()) {
            return super.methodInvoker(frameworkMethod, test);
        }

        if  (       SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()
                || testExecutionContext.testExecutionUsesOneJVM()
            ) {
            QuickPerfMethod quickPerfMethod = new QuickPerfMethod( testMethod
                                                                 , testExecutionContext);
            return super.methodInvoker(quickPerfMethod, test);
        }

        return NO_STATEMENT;
    }

    private int findJUnit4AllocationOffset() {
        int allocationOffsetBeforeJava16 = findAllocationOffsetBeforeJava16();
        JVM.Version jvmVersion = JVM.INSTANCE.version;
        if(!jvmVersion.isGreaterThanOrEqualTo16()) {
            return allocationOffsetBeforeJava16;
        }
        return allocationOffsetBeforeJava16 + 8;
    }

    private int findAllocationOffsetBeforeJava16() {
        JVM.Version jvmVersion = JVM.INSTANCE.version;
        if (jvmVersion.isGreaterThanOrEqualTo12() && !junit4_13IsUsed()) {
            return 72;
        }
        return 40;
    }

    private boolean junit4_13IsUsed() {
        String junit4Version = Version.id();
        return junit4Version.startsWith("4.13");
    }

    @Override
    public Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {

        if (       SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()
                || testExecutionContext.testExecutionUsesOneJVM()
                || testExecutionContext.isQuickPerfDisabled()
           ) {
            return super.withBefores(frameworkMethod, testInstance, statement);
        }

        return NO_STATEMENT;

    }

    @Override
    public Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        Statement junitAfters = super.withAfters(frameworkMethod, testInstance, statement);
        if(   SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()
           || testExecutionContext.isQuickPerfDisabled() ) {
            return junitAfters;
        }
        return new MainJvmAfterJUnitStatement(  frameworkMethod
                                              , testExecutionContext
                                              , quickPerfConfigs
                                              , junitAfters);
    }

}