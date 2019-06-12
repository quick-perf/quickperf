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

package org.quickperf.testlauncher;

import org.apache.commons.io.IOUtils;
import org.quickperf.SystemProperties;
import org.quickperf.WorkingFolder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NewJvmTestLauncher {

    public static final NewJvmTestLauncher INSTANCE = new NewJvmTestLauncher();

    private NewJvmTestLauncher() { }

    public void run(Method testMethod
                  , WorkingFolder workingFolder
                  , AllJvmOptions jvmOptions
                  , Class<?> mainClassToLaunchTestInANewJvm) {

        String className = retrieveClassNameFrom(testMethod);
        String methodName = testMethod.getName();

        List<String> jvmOptionsAsStrings = jvmOptions.asStrings(workingFolder);

        List<String> jvmCommand
                = buildCommandOfLaunchingJUnitCoreInANewJvm(
                          className
                        , methodName
                        , jvmOptionsAsStrings
                        , workingFolder.getPath()
                        , mainClassToLaunchTestInANewJvm);

        CmdExecutionResult cmdExecutionResult  = tryWith(jvmCommand, workingFolder);

        System.out.println(cmdExecutionResult.getMessage());

        if(!cmdExecutionResult.isCommandSuccessful()) {
            System.err.println("Unable to run test in a new JMV: " + cmdExecutionResult.getErrorMessage());
        }

    }

    private static List<String> buildCommandOfLaunchingJUnitCoreInANewJvm(
              String className
            , String methodName
            , List<String> jvmParamsAsString
            , String workingFolderPath
            , Class<?> mainClassToLaunchTest) {
        List<String> command = new ArrayList<>();
        command.add(retrieveJavaExePath());
        command.addAll(jvmParamsAsString);
        command.add(SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM
                                .buildForJvm("true")
                   );
        command.add(SystemProperties.WORKING_FOLDER
                                .buildForJvm(workingFolderPath)
                   );
        command.add("-cp");
        command.add(retrieveCurrentClassPath());
        command.add(mainClassToLaunchTest.getCanonicalName());
        command.add(className);
        command.add(methodName);
        command.add(workingFolderPath);
        return command;
    }

    private static String retrieveClassNameFrom(Method method) {
        Class<?> classOfTestMethod = method.getDeclaringClass();
        return classOfTestMethod.getCanonicalName();
    }

    private static String retrieveJavaExePath() {
        String javaHomeDirectoryPath = System.getProperty("java.home");
        return    javaHomeDirectoryPath
                + File.separator + "bin"
                + File.separator + "java";
    }

    private static String retrieveCurrentClassPath() {
        return System.getProperty("java.class.path");
    }

    private static class CmdExecutionResult {

        private final boolean isCommandSuccessful;

        private final String message;

        private final String errorMessage;

        private CmdExecutionResult(boolean isCommandSuccessful, String message, String errorMessage) {
            this.isCommandSuccessful = isCommandSuccessful;
            this.message = message;
            this.errorMessage = errorMessage;
        }

        private boolean isCommandSuccessful() {
            return isCommandSuccessful;
        }

        private String getMessage() {
            return message;
        }

        private String getErrorMessage() {
            return errorMessage;
        }

    }

    private static CmdExecutionResult tryWith(List<String> cmd, WorkingFolder workingFolder) {

        try {
            final Process process = new ProcessBuilder(cmd).start();

            final StringWriter messageWriter = new StringWriter();
            final StringWriter errorWriter = new StringWriter();

            Thread outDrainer = new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getInputStream(), messageWriter);
                    } catch (IOException e) {
                    }
                }
            });

            Thread errorDrainer = new Thread(new Runnable() {
                public void run() {
                    try {
                        IOUtils.copy(process.getErrorStream(), errorWriter);
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
                return new CmdExecutionResult(false, "", errorMessage);
            }

            String message = messageWriter.toString();
            return new CmdExecutionResult(true, message, "");
        } catch (IOException | InterruptedException ex) {
            return new CmdExecutionResult(false, "", ex.getMessage());
        }

    }

}
