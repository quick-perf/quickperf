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

package org.quickperf.sql.framework.quickperf;

import org.quickperf.sql.framework.ClassPath;
import org.quickperf.sql.framework.QuickPerfSuggestion;

public class DataSourceFrameworkConfigFactory {

    private DataSourceFrameworkConfigFactory() { }

    public static QuickPerfSuggestion makeFrom(ClassPath classPath) {

        if(classPath.containsSpringCore()) {
            return new SpringDataSourceConfig(classPath);
        }
        if(classPath.containsQuarkusCore()) {
            return new QuarkusDataSourceConfig();
        }
        if(classPath.containsMicronautData()) {
            return new MicronautDataConfig();
        }
        if(classPath.containsMicronautHibernateJpa()) {
            return new MicronautHibernateJpaConfig();
        }

        return new DefaultConfig();

    }

}
