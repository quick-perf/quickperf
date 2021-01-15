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

package org.quickperf;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class AnnotationFormatter {

    public static final AnnotationFormatter INSTANCE = new AnnotationFormatter();

    private AnnotationFormatter() {}

    private final Set<String> proxyMethodNames = retrieveMethodNamesOf(Proxy.class);

    private final Set<String> annotationMethodNames = retrieveMethodNamesOf(Annotation.class);

    private Set<String> retrieveMethodNamesOf(Class<?> clazz) {
        Set<String> annotationMethodNames = new HashSet<>();
        Method[] annotationMethods = clazz.getMethods();
        for (Method annotationMethod : annotationMethods) {
            annotationMethodNames.add(annotationMethod.getName());
        }
        return annotationMethodNames;
    }

    public String format(Annotation perfAnnotation) {
        return "@" + retrieveAnnotationNameOf(perfAnnotation)
                + formatParams(perfAnnotation);
    }

    private String retrieveAnnotationNameOf(Annotation perfAnnotation) {
        return perfAnnotation.annotationType().getSimpleName();
    }

    private String formatParams(Annotation perfAnnotation) {

        Method[] methods = perfAnnotation.getClass().getMethods();
        List<Method> filteredMethods = filterMethods(methods);

        if(filteredMethods.isEmpty()) {
            return "";
        }

        String annotationParamsAsString = "";

        for (int i = 0; i < filteredMethods.size(); i++) {

            if( i == 0) {
                annotationParamsAsString += "(";
            } else {
                annotationParamsAsString += ", ";
            }

            Method method = filteredMethods.get(i);
            String methodName = method.getName();

            annotationParamsAsString += methodName
                    + "=" + evaluateParamValue(perfAnnotation, method);

        }

        return annotationParamsAsString + ")";

    }

    private List<Method> filterMethods(Method[] methods) {
        List<Method> filteredMethods = new ArrayList<>();
        for (Method method : methods) {
            if (!isMethodToSkip(method)) {
                filteredMethods.add(method);
            }
        }
        return filteredMethods;
    }

    private boolean isMethodToSkip(Method method) {
        return     proxyMethodNames.contains(method.getName())
                || annotationMethodNames.contains(method.getName());
    }

    private String evaluateParamValue(Annotation perfAnnotation, Method method) {
        // To have the ability to invoke a method of an anonymous class.
        // Configuration classes implementing SpecifiableGlobalAnnotations
        // use anonymous classes.
        method.setAccessible(true);

        Object invoke;
        try {
            invoke = method.invoke(perfAnnotation, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        return invoke.toString();
    }

}
