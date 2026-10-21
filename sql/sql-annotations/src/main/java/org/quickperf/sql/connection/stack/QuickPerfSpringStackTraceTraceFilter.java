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
package org.quickperf.sql.connection.stack;

public class QuickPerfSpringStackTraceTraceFilter implements StackTraceFilter {

    public static final QuickPerfSpringStackTraceTraceFilter INSTANCE = new QuickPerfSpringStackTraceTraceFilter();

    @Override
    public PositionsFiltering filter(StackTraceElement[] stackElements) {
        int springFirstPos = extractSpringFirstPos(stackElements);
        int lastStackElementPos = stackElements.length;
        return new PositionsFiltering(springFirstPos, lastStackElementPos);
    }

    private int extractSpringFirstPos(StackTraceElement[] stackElements) {
        for (int i = 0, elementsLength = stackElements.length; i < elementsLength; i++) {
            StackTraceElement element = stackElements[i];
            if (element.toString().startsWith("org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor$ProxyDataSourceInterceptor.invoke")) {
                return skipSpringAopElements(stackElements, i + 1);
            }
        }
        return 0;
    }

    // The Spring AOP elements are skipped by package because their number depends on the Spring version.
    // CGLIB proxy, Spring 3.2 to 5.1 and Spring 6.2+:
    //org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
    //org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688)
    // CGLIB proxy, Spring 5.2 to 6.1:
    //org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
    //org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation.proceed(CglibAopProxy.java:768)
    //org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:720)
    // JDK dynamic proxy:
    //org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:184)
    //org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:223)
    private int skipSpringAopElements(StackTraceElement[] stackElements, int fromPos) {
        int pos = fromPos;
        while (pos < stackElements.length && stackElements[pos].getClassName().startsWith("org.springframework.aop.")) {
            pos++;
        }
        return pos;
    }

}
