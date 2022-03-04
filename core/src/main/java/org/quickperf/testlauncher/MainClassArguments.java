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
package org.quickperf.testlauncher;

import org.quickperf.WorkingFolder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainClassArguments {

    private final String className;

    private final String methodName;

    private final String workingFolderPath;

    private MainClassArguments(String className, String methodName, String workingFolderPath) {
        this.className = className;
        this.methodName = methodName;
        this.workingFolderPath = workingFolderPath;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getWorkingFolderPath() {
        return workingFolderPath;
    }

    public static MainClassArguments buildFrom(Method testMethod
                                             , WorkingFolder workingFolder) {
        String className = retrieveClassNameOf(testMethod);
        String methodName = testMethod.getName();
        String workingFolderPath = workingFolder.getPath();
        return new MainClassArguments(className, methodName, workingFolderPath);
    }

    private static String retrieveClassNameOf(Method method) {
        Class<?> classOfTestMethod = method.getDeclaringClass();
        return classOfTestMethod.getName();
    }

    public static MainClassArguments buildFromMainArguments(String... args) {
        String className = args[0];
        String methodName = args[1];
        String workingFolderPath = args[2];
        return new MainClassArguments(className, methodName, workingFolderPath);
    }

    public List<String> buildMainClassArgumentsForJvmCommand() {
        List<String> arguments = new ArrayList<>(3);
        arguments.add(className);
        arguments.add(methodName);
        arguments.add(workingFolderPath);
        return arguments;
    }

}