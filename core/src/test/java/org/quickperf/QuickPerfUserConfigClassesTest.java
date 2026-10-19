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
package org.quickperf;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class QuickPerfUserConfigClassesTest {

    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    public static class UserConfigClass {}

    @Test public void
    should_find_a_user_config_class_in_a_directory_whose_path_contains_a_plus_sign() throws Exception {

        // GIVEN
        File classPathDirectory = createClassPathDirectoryContainingUserConfigClass("my+project");
        // file:/.../my+project/, as with -cp, Gradle, and the JVM forked by QuickPerf
        URL classPathUrl = classPathDirectory.toURI().toURL();

        // WHEN
        Class[] classes = findClassesWithClassPath(classPathUrl);

        // THEN
        assertThat(classes).containsExactly(UserConfigClass.class);

    }

    @Test public void
    should_find_a_user_config_class_when_the_plus_sign_of_the_directory_url_is_encoded() throws Exception {

        // GIVEN
        File classPathDirectory = createClassPathDirectoryContainingUserConfigClass("my+project");
        // file:/.../my%2Bproject/, as with the manifest-only JAR of Maven Surefire 3 on Windows
        URL classPathUrl = new URL(classPathDirectory.toURI().toString().replace("+", "%2B"));

        // WHEN
        Class[] classes = findClassesWithClassPath(classPathUrl);

        // THEN
        assertThat(classes).containsExactly(UserConfigClass.class);

    }

    @Test public void
    should_find_a_user_config_class_in_a_directory_whose_path_contains_a_space() throws Exception {

        // GIVEN
        File classPathDirectory = createClassPathDirectoryContainingUserConfigClass("my project");
        // file:/.../my%20project/
        URL classPathUrl = classPathDirectory.toURI().toURL();

        // WHEN
        Class[] classes = findClassesWithClassPath(classPathUrl);

        // THEN
        assertThat(classes).containsExactly(UserConfigClass.class);

    }

    private File createClassPathDirectoryContainingUserConfigClass(String directoryName) throws IOException {
        File classPathDirectory = temporaryFolder.newFolder(directoryName);
        String classFilePath = UserConfigClass.class.getName().replace('.', '/') + ".class";
        File classFile = new File(classPathDirectory, classFilePath);
        classFile.getParentFile().mkdirs();
        try (InputStream classFileContent = UserConfigClass.class.getClassLoader().getResourceAsStream(classFilePath)) {
            Files.copy(classFileContent, classFile.toPath());
        }
        return classPathDirectory;
    }

    private Class[] findClassesWithClassPath(URL classPathUrl) throws Exception {
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        // Without a parent, the class loader only finds the org/quickperf directory of classPathUrl
        URLClassLoader classLoader = new URLClassLoader(new URL[] {classPathUrl}, null);
        currentThread.setContextClassLoader(classLoader);
        try {
            return QuickPerfUserConfigClasses.INSTANCE.findClasses();
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
            classLoader.close();
        }
    }

}
