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

package org.quickperf.sql.formatter;

import org.quickperf.QuickPerfUserConfigClasses;
import org.quickperf.SystemProperties;
import org.quickperf.sql.SqlFormatter;
import org.quickperf.sql.config.SpecifiableSqlFormatter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class SqlFormatterDefinedByUserRetriever {

    static final SqlFormatterDefinedByUserRetriever INSTANCE = new SqlFormatterDefinedByUserRetriever();

    private SqlFormatterDefinedByUserRetriever() {}

    SqlFormatter retrieveSqlFormatterDefinedByUser() {
        SpecifiableSqlFormatter sqlFormatterImpl = null;
        try {
            sqlFormatterImpl = SqlFormatterDefinedByUserRetriever.INSTANCE.findClassImplementingSpecifiableSqlFormatter();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (sqlFormatterImpl != null) {
            return sqlFormatterImpl.specifySqlFormatter();
        }
        return SqlFormatter.NONE;
    }

    private SpecifiableSqlFormatter findClassImplementingSpecifiableSqlFormatter() throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {

        QuickPerfUserConfigClasses quickPerfUserConfigClasses = QuickPerfUserConfigClasses.INSTANCE;

        Class[] classes = quickPerfUserConfigClasses.findClasses();
        for (Class clazz : classes) {
            Class[] interfaces = clazz.getInterfaces();
            for (Class interfaceClass : interfaces) {
                if (isSpecifiableSqlFormatterImpl(interfaceClass)) {
                    if (!SystemProperties.TEST_CODE_EXECUTING_IN_NEW_JVM.evaluate()) {
                        System.out.println("A class specifying a SQL basicFormatter has been found: " + clazz.getCanonicalName());
                    }
                    try {
                        return instantiateSpecifiableSqlFormatter(clazz);
                    } catch (InvocationTargetException | NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;

    }

    @SuppressWarnings("unchecked")
    private SpecifiableSqlFormatter instantiateSpecifiableSqlFormatter(Class clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (SpecifiableSqlFormatter) clazz.getDeclaredConstructor()
                                         .newInstance();
    }

    private boolean isSpecifiableSqlFormatterImpl(Class interfaceClass) {
        return interfaceClass.getCanonicalName().equals(SpecifiableSqlFormatter.class.getCanonicalName());
    }

}
