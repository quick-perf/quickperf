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

package org.quickperf.testlauncher;

import org.apache.commons.io.IOUtils;
import org.quickperf.SystemProperties;
import org.quickperf.TestExecutionContext;
import org.quickperf.WorkingFolder;
import org.quickperf.issue.JvmIssue;
import org.quickperf.issue.TestIssue;
import org.quickperf.issue.JvmOrTestIssue;
import org.quickperf.repository.TestIssueRepository;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class NewJvmTestLauncher {

    public static final NewJvmTestLauncher INSTANCE = new NewJvmTestLauncher();

    private final TestIssueRepository testIssueRepository = TestIssueRepository.INSTANCE;

    private NewJvmTestLauncher() { }

    public JvmOrTestIssue executeTestMethodInNewJwm(Method testMethod
                                                             , TestExecutionContext testExecutionContext
                                                             , Class<?> mainClassToLaunchTestInANewJvm) {

        JvmIssue jvmIssue = executeTestInNewJvm(testMethod
                                              , testExecutionContext
                                              , mainClassToLaunchTestInANewJvm);
        if (!jvmIssue.isNone()) {
            return JvmOrTestIssue.buildFrom(jvmIssue);
        }

        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        TestIssue testIssue = testIssueRepository.findFrom(workingFolder);

        return JvmOrTestIssue.buildFrom(testIssue);

    }

    private JvmIssue executeTestInNewJvm(Method testMethod
                                       , TestExecutionContext testExecutionContext
                                       , Class<?> mainClassToLaunchTestInANewJvm) {

        WorkingFolder workingFolder = testExecutionContext.getWorkingFolder();
        MainClassArguments mainClassArguments = MainClassArguments.buildFrom(testMethod, workingFolder);

        AllJvmOptions jvmOptions = testExecutionContext.getJvmOptions();

        List<String> jvmOptionsAsStrings = jvmOptions.asStrings(workingFolder);

        List<String> jvmCommand = buildCommand( mainClassArguments
                                              , jvmOptionsAsStrings
                                              , workingFolder.getPath()
                                              , mainClassToLaunchTestInANewJvm);

        return execute(jvmCommand);

    }

    private List<String> buildCommand(MainClassArguments mainClassArguments
                                    , List<String> jvmOptionsAsStrings
                                    , String workingFolderPath
                                    , Class<?> mainClassToLaunchTest) {
        List<String> command = new ArrayList<>();
        command.add(retrieveJavaExePath());
        command.addAll(jvmOptionsAsStrings);
        command.add(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM
                                    .buildForJvm("true")
                   );
        command.add(SystemProperties.WORKING_FOLDER
                                    .buildForJvm(workingFolderPath)
                   );
        command.add("-cp");
        command.add(retrieveCurrentClassPath());
        command.add(mainClassToLaunchTest.getCanonicalName());
        List<String> mainClassArgumentsAsStringList = mainClassArguments.buildMainClassArgumentsForJvmCommand();
        command.addAll(mainClassArgumentsAsStringList);
        return command;
    }

    private String retrieveJavaExePath() {
        String javaHomeDirectoryPath = System.getProperty("java.home");
        return    javaHomeDirectoryPath
                + File.separator + "bin"
                + File.separator + "java";
    }

    private String retrieveCurrentClassPath() {
        return System.getProperty("java.class.path");
    }

    private JvmIssue execute(List<String> cmd) {

        try {
            final Process process = new ProcessBuilder(cmd).start();

            final StringWriter messageWriter = new StringWriter();
            final StringWriter errorWriter = new StringWriter();

            Thread outDrainer = new Thread(new Runnable() {
                public void run() {
                    try {
                        Charset defaultCharset = Charset.defaultCharset();
                        IOUtils.copy(process.getInputStream(), messageWriter, defaultCharset);
                    } catch (IOException e) {
                    }
                }
            });

            Thread errorDrainer = new Thread(new Runnable() {
                public void run() {
                    try {
                        Charset defaultCharset = Charset.defaultCharset();
                        IOUtils.copy(process.getErrorStream(), errorWriter, defaultCharset);
                    } catch (IOException e) {
                    }
                }
            });

            outDrainer.start();
            errorDrainer.start();

            int err = process.waitFor();

            outDrainer.join();
            errorDrainer.join();

            if (err != 0) {
                String errorMessage = errorWriter.toString();
                if(errorMessage.isEmpty()) {
                    errorMessage = messageWriter.toString();
                }
                return JvmIssue.buildFrom(errorMessage);
            }

            String message = messageWriter.toString();
            if(!message.isEmpty()) {
                System.out.println(message);
            }

            return JvmIssue.NONE;

        } catch (IOException | InterruptedException e) {
            return JvmIssue.buildFrom(e);
        }

    }

}
