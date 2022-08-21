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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

class ClassPathUtil {

    private static final int DEFAULT_CLASS_PATH_JAR_THRESHOLD = 1024 * 2;

    private static final String ATTRIBUTE_MANIFEST_VERSION = "1.0";

    private static final String MANIFEST_CLASS_PATH_SEPARATOR = " ";

    private ClassPathUtil() {
    }

    static String retrieveCurrentClassPath(String workingFolderPath) throws IOException {

        int classPathJarThreshold = findClassPathJarThreshold();

        String fullClassPath = System.getProperty("java.class.path", "");

        if (fullClassPath.length() < classPathJarThreshold) {
            return fullClassPath;
        } else {
            return createClassPathJarFileAndReturnPath(fullClassPath, workingFolderPath);
        }
    }

    private static int findClassPathJarThreshold() {
        String classPathJarThresholdAsString = System.getProperty("quickperf.classPathJarThreshold");
        if (classPathJarThresholdAsString != null) {
            return Integer.parseInt(classPathJarThresholdAsString);
        }
        return DEFAULT_CLASS_PATH_JAR_THRESHOLD;
    }

    public static String createClassPathJarFileAndReturnPath(String fullClassPath, String workingFolderPath) throws IOException {

        File classPathJar = new File(workingFolderPath + File.separator + "class_path.jar");

        Manifest manifest = buildManifestFrom(fullClassPath);

        createJarFrom(classPathJar, manifest);

        return classPathJar.getAbsolutePath();

    }

    private static Manifest buildManifestFrom(String fullClassPath) throws MalformedURLException {
        String classPath = formatClassPathForManifest(fullClassPath);
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, ATTRIBUTE_MANIFEST_VERSION);
        mainAttributes.put(Attributes.Name.CLASS_PATH, classPath);
        return manifest;
    }

    private static String formatClassPathForManifest(String fullClassPath) throws MalformedURLException {
        String[] paths = fullClassPath.split(File.pathSeparator);
        String classPath = "";
        for (String path : paths) {
            if (classPath.length() > 0) {
                classPath += MANIFEST_CLASS_PATH_SEPARATOR;
            }
            classPath += new File(path).toURI().toURL().toString();
        }
        return classPath;
    }

    private static void createJarFrom(File classPathJar, Manifest manifest) throws IOException {
        try (
                FileOutputStream fos = new FileOutputStream(classPathJar);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                JarOutputStream jar = new JarOutputStream(bos, manifest);
        ) {
            //inspection IO resource opened are safely closed
            jar.flush();
        } catch (IOException io) {
            throw io;
        }
    }
}
