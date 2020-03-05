/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 *
 * Copyright 2019-2020 the original author or authors.
 */

package org.quickperf;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class QuickPerfUserConfigClasses {

    public static final QuickPerfUserConfigClasses INSTANCE = new QuickPerfUserConfigClasses();

    private static final String ORG_QUICK_PERF_PACKAGE = "org.quickperf";

    private QuickPerfUserConfigClasses() {}

    public Class[] findClasses() throws ClassNotFoundException, IOException {
        List<Class> allClasses = new ArrayList<>();
        for (File directory : findDirectoriesForPackage()) {

            List<Class> classesInDirectory = findClasses(directory);
            allClasses.addAll(classesInDirectory);
        }
        return allClasses.toArray(new Class[allClasses.size()]);
    }

    private List<File> findDirectoriesForPackage() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = ORG_QUICK_PERF_PACKAGE.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);

        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String classPath = URLDecoder.decode(resource.getPath(), "UTF-8");
            File file = new File(classPath);
            dirs.add(file);
        }
        return dirs;
    }

    private List<Class> findClasses(File directory) throws ClassNotFoundException {
        if (!directory.exists()) {
            return Collections.emptyList();
        }
        List<Class> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        for (File file : files) {
            String filename = file.getName();
            if (filename.endsWith(".class")) {
                String fileName = filename.substring(0, filename.length() - 6);
                Class clazz = Class.forName(ORG_QUICK_PERF_PACKAGE + '.' + fileName);
                classes.add(clazz);
            }
        }
        return classes;
    }

}
