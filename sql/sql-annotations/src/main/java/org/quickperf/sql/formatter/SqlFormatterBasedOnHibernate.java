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

import org.quickperf.sql.SqlFormatter;
import org.quickperf.sql.framework.ClassPath;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

class SqlFormatterBasedOnHibernate implements SqlFormatter {

   static final SqlFormatter INSTANCE = buildSqlFormatterBasedOnHibernate();

   private Object basicFormatter;

   private Method formatMethod;

   private Object ddlFormatter;

   private SqlFormatterBasedOnHibernate() {
   }

   @SuppressWarnings("unchecked")
   private static SqlFormatter buildSqlFormatterBasedOnHibernate() {
       try {
           Class<? extends Enum> formatStyleClass = retrieveHibernateFormatStyleClass();
           Method getFormatterMethod = formatStyleClass.getDeclaredMethod("getFormatter", null);
           Enum formatStyleBasic = Enum.valueOf(formatStyleClass, "BASIC");
           Object basicFormatter = getFormatterMethod.invoke(formatStyleBasic, null);

           Enum formatStyleDdl = Enum.valueOf(formatStyleClass, "DDL");
           Object ddlFormatter = getFormatterMethod.invoke(formatStyleDdl, null);

           SqlFormatterBasedOnHibernate sqlFormatterBasedOnHibernate = new SqlFormatterBasedOnHibernate();
           sqlFormatterBasedOnHibernate.basicFormatter = basicFormatter;
           sqlFormatterBasedOnHibernate.ddlFormatter = ddlFormatter;

           sqlFormatterBasedOnHibernate.formatMethod = retrieveFormatOfHibernateFormatterClass();

           return sqlFormatterBasedOnHibernate;

       } catch (Exception e) {
           // QuickPerf can't automatically retrieve Hibernate formatters
           // if Hibernate version is less than 4
           return SqlFormatter.NONE;
       }
   }

    @SuppressWarnings("unchecked")
    private static Class<? extends Enum> retrieveHibernateFormatStyleClass() throws ClassNotFoundException {
        ClassLoader loader = SqlFormatterBasedOnHibernate.class.getClassLoader();
        String formatStyleClassName = "org.hibernate.engine.jdbc.internal.FormatStyle";
        return (Class<? extends Enum>) loader.loadClass(formatStyleClassName);
    }

    private static Method retrieveFormatOfHibernateFormatterClass() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> formatterClass = Class.forName("org.hibernate.engine.jdbc.internal.Formatter");
        return formatterClass.getDeclaredMethod("format", String.class);
    }

    @Override
   public String formatQuery(String query) {

       if (ClassPath.INSTANCE.containsHibernate()) {
           try {
               return formatWithHibernateFormatter(query);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }

       return query;
   }

   private String formatWithHibernateFormatter(String query) throws IllegalAccessException, InvocationTargetException {

       if(isDdlQuery(query)) {
           return  (String) formatMethod.invoke(ddlFormatter, query);
       }

       String queryFormattedByHibernate = (String) formatMethod.invoke(basicFormatter, query);

       return queryFormattedByHibernate
             .replaceAll("cross " + System.lineSeparator() + "    "
                         , System.lineSeparator() + "    " + "cross ")
             .replaceAll("CROSS " + System.lineSeparator() + "    "
                         , System.lineSeparator() + "    " + "CROSS ")
           ;

   }

   private boolean isDdlQuery(String sql) {
       return     sql.toLowerCase(Locale.ROOT).startsWith("create table")
               || sql.toLowerCase(Locale.ROOT).startsWith("alter table")
               || sql.toLowerCase(Locale.ROOT).startsWith("comment on");
   }

}
