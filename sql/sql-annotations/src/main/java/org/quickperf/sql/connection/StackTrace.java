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

package org.quickperf.sql.connection;

import org.quickperf.sql.config.library.QuickPerfProxyDataSource;
import org.quickperf.sql.framework.ClassPath;

import java.util.Arrays;

class StackTrace {

    private final StackTraceElement[] elements;

    private StackTrace(StackTraceElement[] elements) {
        this.elements = elements;
    }

    static StackTrace of(StackTraceElement[] elements) {
        return new StackTrace(elements);
    }

    StackTraceElement[] getElements() {
        return elements;
    }

    StackTrace filterFrameworks() {

        short firstPos = 0;

       // if(ClassPath.INSTANCE.containsJUnit4()) {

            if(ClassPath.INSTANCE.containsSpringCore()) {

                for (int i = 0, elementsLength = elements.length; i < elementsLength; i++) {
                    StackTraceElement element = elements[i];
                    if (element.toString().startsWith("org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor$ProxyDataSourceInterceptor.invoke")) {
                        //org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
                        //org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688)
                        firstPos = (short) (i +3);
                        break;
                    }
                    firstPos = extractFirstPos(elements);
                }


            } else {
                firstPos = extractFirstPos(elements);
            }

        short lastPos = (short) (extractLastPos(firstPos) + 1);

        StackTraceElement[] newElements = Arrays.copyOfRange(elements, firstPos, lastPos);

        return StackTrace.of(newElements);

    }

    private short extractLastPos(short positionOfFirstNonQuickPerfElement) {
        if(containsTestFramework(elements)) {
            short quickPerfJUnit4MethodPos = extractQuickPerfJUnit4JUnit5TestNGMethodPos(positionOfFirstNonQuickPerfElement);
            for (short pos = (short) (quickPerfJUnit4MethodPos -1); pos >0 ; pos--) {
                String stackTraceElementAsString = elements[pos].toString();
                if(!stackTraceElementAsString.startsWith("java") && !stackTraceElementAsString.startsWith("org.testng")) {
                    return pos;
                }
            }
        }
        return (short) elements.length;
    }

    private short extractQuickPerfJUnit4JUnit5TestNGMethodPos(short positionOfFirstNonQuickPerfElement) {
        for (short pos = (short) (positionOfFirstNonQuickPerfElement + 1)
             ; pos < elements.length; pos++) {
            String stackTraceElementAsString = elements[pos].toString();
            if(  stackTraceElementAsString.contains("org.quickperf.junit4.QuickPerfMethod")
              || stackTraceElementAsString.contains("org.junit.platform.commons.util.ReflectionUtils.invokeMethod")
              || stackTraceElementAsString.contains("org.quickperf.testng.QuickPerfTestNGListener")) {
                 return pos;
            }
        }
        return (short) 0;
    }

    private boolean containsTestFramework(StackTraceElement[] elements) {
        for (StackTraceElement element : elements) {
            if(element.toString().contains("org.quickperf.junit4.QuickPerfMethod")
            || element.toString().contains("org.junit.platform.commons.util.ReflectionUtils.invokeMethod")
            || element.toString().contains("org.quickperf.testng.QuickPerfTestNGListener")) {
                return true;
            }
        }
        return false;
    }

    private final String quickPerfProxyDataSourcePackage = QuickPerfProxyDataSource.class.getPackage().getName();

    private final String connectionProfilerPackageName = TestConnectionProfiler.class.getPackage().getName();

    private short extractFirstPos(StackTraceElement[] stackTrace) {
        for (short pos = 0; pos < stackTrace.length; pos++) {
            String stackTraceElementAsString = stackTrace[pos].toString();
            if( !stackTraceElementAsString.contains("java.lang.Thread")
             && !stackTraceElementAsString.contains(connectionProfilerPackageName)
             && !stackTraceElementAsString.contains(quickPerfProxyDataSourcePackage)) {
                return pos;
            }
        }
        return 0;
    }

    StackTrace limitDepthTo(StackDepth stackDepth) {
        int maxStackDepthValue = stackDepth.getMaxValue();
        if(elements.length <= maxStackDepthValue) {
            return this;
        }
        return buildStackTraceWith(stackDepth);
    }

    private StackTrace buildStackTraceWith(StackDepth stackDepth) {
        int maxStackDepthValue = stackDepth.getMaxValue();
        StackTraceElement[] newElements = new StackTraceElement[maxStackDepthValue];
        for (int i = 0; i <  elements.length; i++) {
            if (i + 1 <= maxStackDepthValue) {
                newElements[i] = elements[i];
            }
        }
        return StackTrace.of(newElements);
    }

}
