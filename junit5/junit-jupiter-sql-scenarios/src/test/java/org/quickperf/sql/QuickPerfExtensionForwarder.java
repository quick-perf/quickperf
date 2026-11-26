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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Forwards every callback and interception to the QuickPerf extension of the module running the shared scenarios:
 * <code>org.quickperf.junit5.QuickPerfTestExtension</code> in junit5-sql-test, and
 * <code>org.quickperf.junit6.QuickPerfTestExtension</code> in junit6-sql-test. The
 * {@value #EXTENSION_UNDER_TEST_RESOURCE} resource of the module names it, with the {@value #EXTENSION_UNDER_TEST_KEY}
 * key. A resource, unlike a system property, also reaches the JVMs that quick-perf-junit5 starts.
 * <p>
 * Each instance has its own instance of the extension under test, as each registration of the extension would.
 * <p>
 * The name of this class matches none of the test classes that Maven Surefire includes by default: on JUnit 5.6 and
 * 5.7, the discovery of a class whose methods use DynamicTestInvocationContext (JUnit 5.8+) fails. JUnit 5.6 and 5.7
 * call the 2-argument interceptDynamicTest method, and JUnit 5.8 and later, JUnit 6 included, call the 3-argument one.
 * <p>
 * It does not forward TestInstancePreConstructCallback (JUnit 5.9) and BeforeClassTemplateInvocationCallback (JUnit
 * 5.13): implementing them would prevent loading this class on JUnit 5.6. The scenarios do not need these hooks of
 * quick-perf-junit6, which only mark threads.
 *
 * @see QuickPerfUnderTest
 */
class QuickPerfExtensionForwarder implements BeforeAllCallback, BeforeEachCallback, BeforeTestExecutionCallback
                                                  , AfterEachCallback, InvocationInterceptor {

    static final String EXTENSION_UNDER_TEST_RESOURCE = "quickperf-extension-under-test.properties";

    static final String EXTENSION_UNDER_TEST_KEY = "extension";

    private final Extension delegate = instantiateExtensionUnderTest();

    static Class<?> findExtensionUnderTest() {
        ClassLoader classLoader = QuickPerfExtensionForwarder.class.getClassLoader();
        String className = readExtensionUnderTestClassName(classLoader);
        try {
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new ExtensionConfigurationException("The QuickPerf extension under test, " + className
                                                    + ", is not on the class path", e);
        }
    }

    private static String readExtensionUnderTestClassName(ClassLoader classLoader) {
        Properties properties = new Properties();
        try (InputStream resource = classLoader.getResourceAsStream(EXTENSION_UNDER_TEST_RESOURCE)) {
            if (resource != null) {
                properties.load(resource);
            }
        } catch (IOException e) {
            throw new ExtensionConfigurationException("Cannot read " + EXTENSION_UNDER_TEST_RESOURCE, e);
        }
        String className = properties.getProperty(EXTENSION_UNDER_TEST_KEY);
        if (className == null || className.trim().isEmpty()) {
            throw new ExtensionConfigurationException(
                    "The module running the shared SQL scenarios needs a " + EXTENSION_UNDER_TEST_RESOURCE
                  + " test resource naming its QuickPerf extension, for example "
                  + EXTENSION_UNDER_TEST_KEY + "=org.quickperf.junit6.QuickPerfTestExtension");
        }
        return className.trim();
    }

    private static Extension instantiateExtensionUnderTest() {
        Class<?> extensionUnderTest = findExtensionUnderTest();
        try {
            return (Extension) extensionUnderTest.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new ExtensionConfigurationException("Cannot instantiate " + extensionUnderTest.getName(), e);
        } catch (InvocationTargetException e) {
            throw new ExtensionConfigurationException("Cannot instantiate " + extensionUnderTest.getName()
                                                    , e.getTargetException());
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        if (delegate instanceof BeforeAllCallback) {
            ((BeforeAllCallback) delegate).beforeAll(context);
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        if (delegate instanceof BeforeEachCallback) {
            ((BeforeEachCallback) delegate).beforeEach(context);
        }
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) throws Exception {
        if (delegate instanceof BeforeTestExecutionCallback) {
            ((BeforeTestExecutionCallback) delegate).beforeTestExecution(context);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (delegate instanceof AfterEachCallback) {
            ((AfterEachCallback) delegate).afterEach(context);
        }
    }

    @Override
    public <T> T interceptTestClassConstructor(Invocation<T> invocation
                                             , ReflectiveInvocationContext<Constructor<T>> invocationContext
                                             , ExtensionContext extensionContext) throws Throwable {
        return interceptor().interceptTestClassConstructor(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeAllMethod(Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptBeforeAllMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<Void> invocation
                                        , ReflectiveInvocationContext<Method> invocationContext
                                        , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptBeforeEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<Void> invocation
                                  , ReflectiveInvocationContext<Method> invocationContext
                                  , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public <T> T interceptTestFactoryMethod(Invocation<T> invocation
                                          , ReflectiveInvocationContext<Method> invocationContext
                                          , ExtensionContext extensionContext) throws Throwable {
        return interceptor().interceptTestFactoryMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<Void> invocation
                                          , ReflectiveInvocationContext<Method> invocationContext
                                          , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptTestTemplateMethod(invocation, invocationContext, extensionContext);
    }

    // Called by JUnit 5.6 and 5.7 only
    @SuppressWarnings("deprecation")
    @Override
    public void interceptDynamicTest(Invocation<Void> invocation, ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptDynamicTest(invocation, extensionContext);
    }

    // Called by JUnit 5.8 and later, JUnit 6 included. Its default implementation calls the 2-argument method, which
    // JUnit 6 no longer has: it must not call super.
    @Override
    public void interceptDynamicTest(Invocation<Void> invocation
                                   , DynamicTestInvocationContext invocationContext
                                   , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptDynamicTest(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterEachMethod(Invocation<Void> invocation
                                       , ReflectiveInvocationContext<Method> invocationContext
                                       , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptAfterEachMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptAfterAllMethod(Invocation<Void> invocation
                                      , ReflectiveInvocationContext<Method> invocationContext
                                      , ExtensionContext extensionContext) throws Throwable {
        interceptor().interceptAfterAllMethod(invocation, invocationContext, extensionContext);
    }

    private InvocationInterceptor interceptor() {
        return (InvocationInterceptor) delegate;
    }

}
