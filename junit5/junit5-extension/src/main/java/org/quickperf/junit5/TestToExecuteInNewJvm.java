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
package org.quickperf.junit5;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.engine.UniqueId;
import org.quickperf.WorkingFolder;
import org.quickperf.repository.ObjectFileRepository;

import java.io.File;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The test JVM saves the JUnit unique id of the test in the working folder, and the new JVM executes the test having
 * this unique id. The unique id of a test template invocation contains its index, for example
 * <code>[test-template-invocation:#2]</code>: so the arguments source of a parameterized test has to provide the same
 * arguments, in the same order, in every JVM. Likewise, the unique id of a dynamic test contains its index and the
 * indexes of its dynamic containers, for example <code>[dynamic-container:#1]/[dynamic-test:#2]</code>: the new JVM
 * executes the test factory method again, and then only this dynamic test. So the test factory method has to provide
 * the same dynamic tests and dynamic containers, in the same order, in every JVM. The other dynamic tests of the test
 * factory are not executed in the new JVM: a dynamic test cannot depend on the dynamic tests executed before it.
 * <p>
 * The test JVM also saves the display name of the test: the new JVM fails the test when the dynamic test having the
 * unique id has another display name there. Likewise, it saves the display names of the invocations of parameterized
 * tests and parameterized classes, which contain their arguments by default: the new JVM fails the test when one of
 * these invocations has another display name there.
 */
class TestToExecuteInNewJvm implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String FILE_NAME = "junit5-test-to-execute.ser";

    private static final String TEST_TEMPLATE_INVOCATION_SEGMENT_TYPE = "test-template-invocation";

    private static final String CLASS_TEMPLATE_INVOCATION_SEGMENT_TYPE = "class-template-invocation";

    private static final String PARAMETERIZED_TEST_ANNOTATION = "org.junit.jupiter.params.ParameterizedTest";

    private static final String PARAMETERIZED_CLASS_ANNOTATION = "org.junit.jupiter.params.ParameterizedClass";

    // The new JVM does not get the configuration parameters of the test JVM given by a build tool, a system property
    // or a suite. Without them, it would build other display names.
    private static final List<String> DISPLAY_NAME_CONFIGURATION_PARAMETERS =
            Arrays.asList("junit.jupiter.displayname.generator.default"
                        , "junit.jupiter.params.displayname.default"
                        , "junit.jupiter.params.displayname.argument.maxlength");

    private final String uniqueId;

    private final String displayName;

    // The display names of the invocations of parameterized tests and parameterized classes of the test and of its
    // ancestors, by unique id, from the outermost one
    private final LinkedHashMap<String, String> parameterizedInvocationDisplayNames;

    private final HashMap<String, String> displayNameConfigurationParameters;

    private TestToExecuteInNewJvm(String uniqueId
                                , String displayName
                                , LinkedHashMap<String, String> parameterizedInvocationDisplayNames
                                , HashMap<String, String> displayNameConfigurationParameters) {
        this.uniqueId = uniqueId;
        this.displayName = displayName;
        this.parameterizedInvocationDisplayNames = parameterizedInvocationDisplayNames;
        this.displayNameConfigurationParameters = displayNameConfigurationParameters;
    }

    static TestToExecuteInNewJvm buildFrom(ExtensionContext extensionContext) {
        return new TestToExecuteInNewJvm(extensionContext.getUniqueId()
                                       , extensionContext.getDisplayName()
                                       , findParameterizedInvocationDisplayNames(extensionContext)
                                       , findDisplayNameConfigurationParameters(extensionContext));
    }

    private static LinkedHashMap<String, String> findParameterizedInvocationDisplayNames(ExtensionContext context) {
        List<ExtensionContext> parameterizedInvocations = new ArrayList<>();
        for (ExtensionContext ancestor = context; ancestor != null; ancestor = ancestor.getParent().orElse(null)) {
            if (isParameterizedInvocation(ancestor)) {
                parameterizedInvocations.add(0, ancestor);
            }
        }
        LinkedHashMap<String, String> displayNameByUniqueId = new LinkedHashMap<>();
        for (ExtensionContext parameterizedInvocation : parameterizedInvocations) {
            displayNameByUniqueId.put(parameterizedInvocation.getUniqueId(), parameterizedInvocation.getDisplayName());
        }
        return displayNameByUniqueId;
    }

    // JUnit builds the display name of an invocation of a @ParameterizedTest or of a @ParameterizedClass from its
    // arguments. The other invocations are not checked: the ones of a @RepeatedTest have no arguments, and the display
    // name of an invocation of a custom TestTemplateInvocationContextProvider or ClassTemplateInvocationContextProvider
    // can depend on the invocations created before it, with a counter for example, which the new JVM does not create.
    private static boolean isParameterizedInvocation(ExtensionContext context) {
        String segmentType = UniqueId.parse(context.getUniqueId()).getLastSegment().getType();
        ExtensionContext template = context.getParent().orElse(null);
        if (template == null) {
            return false;
        }
        if (TEST_TEMPLATE_INVOCATION_SEGMENT_TYPE.equals(segmentType)) {
            Method testMethod = template.getTestMethod().orElse(null);
            return testMethod != null
                && isAnnotated(testMethod, testMethod.getDeclaringClass(), PARAMETERIZED_TEST_ANNOTATION);
        }
        if (CLASS_TEMPLATE_INVOCATION_SEGMENT_TYPE.equals(segmentType)) {
            Class<?> testClass = template.getTestClass().orElse(null);
            return testClass != null && isAnnotated(testClass, testClass, PARAMETERIZED_CLASS_ANNOTATION);
        }
        return false;
    }

    // junit-jupiter-params is not a dependency of QuickPerf, and @ParameterizedClass exists from JUnit 5.13
    private static boolean isAnnotated(AnnotatedElement element, Class<?> elementClass, String annotationTypeName) {
        try {
            Class<?> annotationType = Class.forName(annotationTypeName, false, elementClass.getClassLoader());
            return AnnotationSupport.isAnnotated(element, annotationType.asSubclass(Annotation.class));
        } catch (ClassNotFoundException annotationTypeNotOnTheClasspath) {
            return false;
        }
    }

    private static HashMap<String, String> findDisplayNameConfigurationParameters(ExtensionContext extensionContext) {
        HashMap<String, String> displayNameConfigurationParameters = new HashMap<>();
        for (String key : DISPLAY_NAME_CONFIGURATION_PARAMETERS) {
            String value = extensionContext.getConfigurationParameter(key).orElse(null);
            if (value != null) {
                displayNameConfigurationParameters.put(key, value);
            }
        }
        return displayNameConfigurationParameters;
    }

    void saveIn(WorkingFolder workingFolder) {
        ObjectFileRepository.INSTANCE.save(workingFolder, FILE_NAME, this);
    }

    static TestToExecuteInNewJvm findIn(String workingFolderPath) {
        File file = new File(workingFolderPath, FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        return (TestToExecuteInNewJvm) ObjectFileRepository.INSTANCE.find(workingFolderPath, FILE_NAME);
    }

    String getUniqueId() {
        return uniqueId;
    }

    String getDisplayName() {
        return displayName;
    }

    /**
     * The display names, in the test JVM, of the invocations of parameterized tests and parameterized classes of the
     * test and of its ancestors, by unique id, from the outermost one
     */
    Map<String, String> getParameterizedInvocationDisplayNames() {
        return parameterizedInvocationDisplayNames;
    }

    /**
     * The configuration parameters having a value in the test JVM, among the ones that change the display names
     */
    Map<String, String> getDisplayNameConfigurationParameters() {
        return displayNameConfigurationParameters;
    }

}
