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

package org.quickperf.sql.connection.stack;

public class SpringStackTraceTraceFilter implements StackTraceFilter {

    public static final SpringStackTraceTraceFilter INSTANCE = new SpringStackTraceTraceFilter();

    @Override
    public PositionsFiltering filter(StackTraceElement[] stackElements) {
        int springFirstPos = extractSpringFirstPos(stackElements);
        int lastStackElementPos = stackElements.length;
        return new PositionsFiltering(springFirstPos, lastStackElementPos);
    }

    private int extractSpringFirstPos(StackTraceElement[] stackElements) {
        int firstPos = 0;
        for (int i = 0, elementsLength = stackElements.length; i < elementsLength; i++) {
            StackTraceElement element = stackElements[i];
            if (element.toString().startsWith("org.quickperf.spring.sql.QuickPerfProxyBeanPostProcessor$ProxyDataSourceInterceptor.invoke")) {
                //org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:186)
                //org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor.intercept(CglibAopProxy.java:688)
                firstPos = i + 3;
                break;
            }
        }
        return firstPos;
    }

}
