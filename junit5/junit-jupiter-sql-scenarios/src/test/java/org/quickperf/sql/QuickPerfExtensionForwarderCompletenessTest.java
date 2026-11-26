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
package org.quickperf.sql;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The scenarios test the QuickPerf extension through QuickPerfExtensionForwarder: it must forward everything that the
// extension does. This check runs in each module running the shared scenarios, against its extension.
class QuickPerfExtensionForwarderCompletenessTest {

    // The forwarder is compiled against JUnit Jupiter 5.8.2, and runs on 5.6.2, where the class could not be loaded if
    // it implemented an interface of a later version. No scenario needs these hooks of quick-perf-junit6, which only
    // mark threads: its own tests check them, with @QuickPerfTest.
    private static final Set<String> INTERFACES_THAT_THE_FORWARDER_CANNOT_IMPLEMENT = new HashSet<>(Arrays.asList(
              "org.junit.jupiter.api.extension.TestInstancePreConstructCallback"        // JUnit 5.9
            , "org.junit.jupiter.api.extension.BeforeClassTemplateInvocationCallback")); // JUnit 5.13

    @Test
    public void should_implement_every_extension_interface_of_the_extension_under_test() {

        // GIVEN
        Class<?> extensionUnderTest = QuickPerfExtensionForwarder.findExtensionUnderTest();

        // WHEN
        List<Class<?>> missingInterfaces = missingExtensionInterfaces(QuickPerfExtensionForwarder.class
                                                                    , extensionUnderTest);

        // THEN
        assertThat(missingInterfaces).as("extension interfaces of %s", extensionUnderTest.getName())
                                     .isEmpty();

    }

    @Test
    public void should_forward_every_extension_method_that_the_extension_under_test_overrides() {

        // On JUnit 5.6 and 5.7, the reflection on the methods of the forwarder fails: one of them uses
        // DynamicTestInvocationContext (JUnit 5.8+). There, the dynamic-test scenarios fail if the forwarder lacks the
        // 2-argument interceptDynamicTest method.
        assumeTrue(isOnTheClassPath("org.junit.jupiter.api.extension.DynamicTestInvocationContext")
                 , "JUnit Jupiter 5.8 or later");

        // GIVEN
        Class<?> extensionUnderTest = QuickPerfExtensionForwarder.findExtensionUnderTest();

        // WHEN
        List<String> missingOverrides = missingOverrides(QuickPerfExtensionForwarder.class, extensionUnderTest);

        // THEN
        assertThat(missingOverrides).as("extension methods of %s", extensionUnderTest.getName())
                                    .isEmpty();

    }

    @Test
    public void should_report_a_missing_extension_interface() {

        // WHEN
        List<Class<?>> missingInterfaces = missingExtensionInterfaces(ForwarderMissingAnInterface.class
                                                                    , ExtensionWithTwoCallbacks.class);

        // THEN
        assertThat(missingInterfaces).containsExactly(AfterEachCallback.class);

    }

    @Test
    public void should_report_a_missing_override() {

        // WHEN
        List<String> missingOverrides = missingOverrides(ForwarderMissingAnOverride.class
                                                       , InterceptorOverridingTwoMethods.class);

        // THEN
        assertThat(missingOverrides).hasSize(1);
        assertThat(missingOverrides.get(0)).startsWith("interceptTestMethod(");

    }

    private static List<Class<?>> missingExtensionInterfaces(Class<?> forwarder, Class<?> delegate) {
        List<Class<?>> missingInterfaces = new ArrayList<>(forwardableExtensionInterfacesOf(delegate));
        missingInterfaces.removeAll(extensionInterfacesOf(forwarder));
        return missingInterfaces;
    }

    private static List<String> missingOverrides(Class<?> forwarder, Class<?> delegate) {
        Set<Class<?>> extensionInterfaces = forwardableExtensionInterfacesOf(delegate);
        List<String> missingOverrides = new ArrayList<>();
        for (Method method : delegate.getDeclaredMethods()) {
            boolean overridable =    Modifier.isPublic(method.getModifiers())
                                  && !Modifier.isStatic(method.getModifiers())
                                  && !method.isSynthetic()
                                  && !method.isBridge();
            if (   overridable
                && isDeclaredByOneOf(extensionInterfaces, method)
                && !isDeclaredBy(forwarder, method)) {
                missingOverrides.add(method.getName() + Arrays.toString(method.getParameterTypes())
                                                              .replace('[', '(').replace(']', ')'));
            }
        }
        return missingOverrides;
    }

    private static Set<Class<?>> forwardableExtensionInterfacesOf(Class<?> delegate) {
        Set<Class<?>> extensionInterfaces = extensionInterfacesOf(delegate);
        for (Iterator<Class<?>> iterator = extensionInterfaces.iterator(); iterator.hasNext(); ) {
            if (INTERFACES_THAT_THE_FORWARDER_CANNOT_IMPLEMENT.contains(iterator.next().getName())) {
                iterator.remove();
            }
        }
        return extensionInterfaces;
    }

    // The interfaces extending Extension that the type implements, directly or not
    private static Set<Class<?>> extensionInterfacesOf(Class<?> type) {
        Set<Class<?>> extensionInterfaces = new LinkedHashSet<>();
        for (Class<?> clazz = type; clazz != null; clazz = clazz.getSuperclass()) {
            addExtensionInterfaces(clazz.getInterfaces(), extensionInterfaces);
        }
        return extensionInterfaces;
    }

    private static void addExtensionInterfaces(Class<?>[] interfaces, Set<Class<?>> extensionInterfaces) {
        for (Class<?> anInterface : interfaces) {
            if (Extension.class.isAssignableFrom(anInterface) && extensionInterfaces.add(anInterface)) {
                addExtensionInterfaces(anInterface.getInterfaces(), extensionInterfaces);
            }
        }
    }

    private static boolean isDeclaredByOneOf(Set<Class<?>> types, Method method) {
        for (Class<?> type : types) {
            if (isDeclaredBy(type, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDeclaredBy(Class<?> type, Method method) {
        try {
            type.getDeclaredMethod(method.getName(), method.getParameterTypes());
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private static boolean isOnTheClassPath(String className) {
        try {
            Class.forName(className, false, QuickPerfExtensionForwarderCompletenessTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    static class ExtensionWithTwoCallbacks implements BeforeEachCallback, AfterEachCallback {

        @Override
        public void beforeEach(ExtensionContext context) { }

        @Override
        public void afterEach(ExtensionContext context) { }

    }

    static class ForwarderMissingAnInterface implements BeforeEachCallback {

        @Override
        public void beforeEach(ExtensionContext context) { }

    }

    static class InterceptorOverridingTwoMethods implements InvocationInterceptor {

        @Override
        public void interceptBeforeEachMethod(Invocation<Void> invocation
                                            , ReflectiveInvocationContext<Method> invocationContext
                                            , ExtensionContext extensionContext) throws Throwable {
            invocation.proceed();
        }

        @Override
        public void interceptTestMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
            invocation.proceed();
        }

    }

    static class ForwarderMissingAnOverride implements InvocationInterceptor {

        @Override
        public void interceptBeforeEachMethod(Invocation<Void> invocation
                                            , ReflectiveInvocationContext<Method> invocationContext
                                            , ExtensionContext extensionContext) throws Throwable {
            invocation.proceed();
        }

    }

}
